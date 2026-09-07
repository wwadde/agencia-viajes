package com.agencia.chatbot.service;

import com.agencia.chatbot.HistorialAdvisor;
import com.agencia.chatbot.entity.ToolHistorial;
import com.agencia.chatbot.utils.ContextoConversacionTool;
import com.agencia.chatbot.utils.ConversacionMapper;
import com.agencia.chatbot.dto.ChatRequest;
import com.agencia.chatbot.dto.ConversacionDTO;
import com.agencia.chatbot.entity.Conversacion;
import com.agencia.chatbot.entity.RolMensaje;
import com.agencia.chatbot.repository.ConversacionRepository;
import com.agencia.chatbot.utils.ToolCallEvent;
import com.agencia.chatbot.utils.ToolCallObservingAdvisor;
import com.agencia.chatbot.ChatMemoryManager;
import com.agencia.domain.Usuario;
import com.agencia.exception.ChatbotException;
import com.agencia.exception.ConversacionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;
    private final ConversacionRepository conversacionRepository;
    private final GuardarChatService guardarChatService;
    private final ConversacionMapper conversacionMapper;
    private final ChatMemoryManager chatMemoryManager;
    private final HistorialAdvisor historialAdvisor;
    private static final ZoneId colombiaZoneId = ZoneId.of("America/Bogota");

    public static final int MAX_PREGUNTAS = 5;
    public static final String USUARIO_ID_KEY = "usuarioId";


    @Override
    public SseEmitter chat(ChatRequest request, Usuario usuario) {

        log.info("Iniciando chat con usuario {}", usuario.getUsuario());

        // Se limitan los mensajes por día
        LocalDateTime inicioDia = LocalDate.now(colombiaZoneId).atStartOfDay();
        long preguntasDelDia = conversacionRepository.contarMensajesDelDia(usuario.getId(), inicioDia, RolMensaje.USUARIO);
        if (preguntasDelDia >= MAX_PREGUNTAS) {
            throw new ChatbotException("Ha alcanzado el límite de (" + MAX_PREGUNTAS + ") preguntas para hoy. Inténtalo nuevamente mañana.");
        }

        Conversacion conversacion = obtenerOCrearConversacion(request, usuario);
        SseEmitter emitter = new SseEmitter(180_000L);


        // Se limitan los mensajes por conversación
        long preguntasConversacion = conversacionRepository.contarMensajesConversacion(conversacion.getId(), RolMensaje.USUARIO);
        if (preguntasConversacion >= MAX_PREGUNTAS) {
            enviarEvento(emitter, "error", "Ha alcanzado el límite de (" + MAX_PREGUNTAS + ") preguntas para esta conversación");
            emitter.complete();
            return emitter;
        }

        // Envíar id de la conversacion, si no puede, retorna el error
        if (!enviarEvento(emitter, "conversationId", conversacion.getId().toString())) {
            return emitter;
        }

        // Si no existe en memoria, se carga la conversacion desde la base de datos y se vuelve a cargar en chatmemory
        chatMemoryManager.prepararMemoriaConversacion(conversacion.getId());

        // Side channel de tools
        Sinks.Many<String> toolSink = Sinks.many().unicast().onBackpressureBuffer();
        ToolCallObservingAdvisor toolObserver = new ToolCallObservingAdvisor(event -> {
            if (event.phase() == ToolCallEvent.Phase.STARTED) {
                toolSink.tryEmitNext(event.name());
            }
        });

        // Suscribir tools SSE
        Disposable toolSubscription = toolSink.asFlux()
                .subscribe(
                        toolName -> enviarEvento(emitter, "tool", toolName),
                        e -> log.warn("Error en tool sink", e)
                );

        // Se siembra el contexto compartido con lo que ya estaba persistido en metadata.
        // Este MISMO mapa es el que viaja en el toolContext y el que se vuelve a
        // guardar al final del turno — sin transformaciones intermedias.
        List<ToolHistorial> contextoConversacion = new CopyOnWriteArrayList<>();
        if (conversacion.getMetadata() != null) {
            contextoConversacion.addAll(conversacion.getMetadata());
        }

        Map<String, Object> toolContext = Map.of(
                USUARIO_ID_KEY, usuario.getId(),
                ContextoConversacionTool.CLAVE, contextoConversacion
        );

        StringBuilder respuestaCompleta = new StringBuilder();
        AtomicReference<Integer> inputTokens = new AtomicReference<>();
        AtomicReference<Integer> outputTokens = new AtomicReference<>();
        AtomicReference<Integer> totalTokens = new AtomicReference<>();
        AtomicBoolean errorSistema = new AtomicBoolean(false);

        Disposable disposable = chatClient.prompt()
                .user(request.peticion())
                .advisors(a -> a
                        .advisors(toolObserver, historialAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversacion.getId().toString())
                        .param(HistorialAdvisor.HISTORIAL_KEY, contextoConversacion))
                .toolContext(toolContext)
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> procesarChunk(chatResponse, respuestaCompleta, inputTokens, outputTokens, totalTokens, emitter))
                .doOnError(error -> {
                    errorSistema.set(true);
                    log.error("Error en el stream de conversación {}", conversacion.getId(), error);
                    String mensajeUsuario = resolverMensajeError(error);
                    enviarEvento(emitter, "error", mensajeUsuario);
                    emitter.complete();
                })
                .doOnComplete(() -> {
                    if (!respuestaCompleta.isEmpty() && !errorSistema.get()) {

                        conversacion.setMetadata((List<ToolHistorial>) toolContext.get(ContextoConversacionTool.CLAVE));

                        guardarConversacionAsync(
                                conversacion,
                                request.peticion(),
                                respuestaCompleta.toString(),
                                errorSistema.get(),
                                inputTokens.get(),
                                outputTokens.get(),
                                totalTokens.get()
                        );
                    }
                    log.info("Conversación {} finalizada | tokens total={}", conversacion.getId(), totalTokens.get() != null ? totalTokens.get() : null);
                    emitter.complete();
                })
                .subscribe();

        Runnable cleanup = () -> {
            disposable.dispose();
            toolSubscription.dispose();
            toolSink.tryEmitComplete();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(_ -> cleanup.run());


        return emitter;
    }

    @Override
    public ConversacionDTO getConversation(UUID id, Usuario usuario) {
        Conversacion conversacion = conversacionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ConversacionException("Conversación " + id + " no encontrada"));

        return conversacionMapper.toResponse(conversacion);
    }

    @Override
    public Page<ConversacionDTO> getConversaciones(Long usuarioId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaActualizacion"));
        return conversacionRepository
                .findByUsuarioId(usuarioId, pageable)
                .map(conversacionMapper::toResponseSinMensajes);
    }


    private Conversacion obtenerOCrearConversacion(ChatRequest request, Usuario usuario) {
        return conversacionRepository.conversacionExiste(request.conversacionId(), usuario.getId())
                .orElseGet(() -> {
                    LocalDateTime fecha = LocalDateTime.now(colombiaZoneId);
                    return conversacionRepository.save(Conversacion.builder()
                            .titulo(StringUtils.abbreviate(request.peticion(), 60)
                                    .replace("\n", " ")
                                    .trim())
                            .usuario(usuario)
                            .fechaCreacion(fecha)
                            .fechaActualizacion(fecha)
                            .build());
                });
    }


    private void guardarConversacionAsync(Conversacion conversacion, String pregunta, String respuesta, boolean errorSistema,
                                          Integer inputTokens, Integer outputTokens, Integer totalTokens) {

        Mono.fromRunnable(() -> guardarChatService.guardarRespuestas(respuesta, pregunta, conversacion, errorSistema, inputTokens, outputTokens, totalTokens))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> log.error("Error guardando conversación {}", conversacion.getId(), error)
                );
    }

    private boolean enviarEvento(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name(event)
                            .data(data)
            );
            return true;
        } catch (IOException e) {
            emitter.completeWithError(e);
            return false;
        }
    }

    private void procesarChunk(ChatResponse chatResponse, StringBuilder respuestaCompleta, AtomicReference<Integer> inputTokens,
                               AtomicReference<Integer> outputTokens, AtomicReference<Integer> totalTokens, SseEmitter emitter) {
        if (chatResponse == null) return;

        String texto = Optional.ofNullable(chatResponse.getResult())
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .orElse(null);

        if (texto != null && !texto.isBlank()) {
            respuestaCompleta.append(texto);
            enviarEvento(emitter, "message", texto);
        }

        Usage usage = Optional.ofNullable(chatResponse.getMetadata())
                .map(ChatResponseMetadata::getUsage)
                .orElse(null);

        if (usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0) {
            Optional.ofNullable(usage.getPromptTokens()).ifPresent(inputTokens::set);
            Optional.ofNullable(usage.getCompletionTokens()).ifPresent(outputTokens::set);
            totalTokens.set(usage.getTotalTokens());
        }
    }

    private String resolverMensajeError(Throwable error) {
        // Desenrollamos posibles wrappers de Reactor / Spring
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        // Error específico de Google GenAI
        if (root instanceof com.google.genai.errors.ServerException serverEx) {
            String msg = serverEx.getMessage() != null ? serverEx.getMessage() : "";

            if (msg.contains("503") || msg.toLowerCase().contains("high demand")) {
                return "El modelo de IA está temporalmente saturado (alta demanda). "
                       + "Por favor intenta de nuevo en unos segundos.";
            }
            if (msg.contains("429") || msg.toLowerCase().contains("rate limit")
                || msg.toLowerCase().contains("quota")) {
                return "Se alcanzó el límite de uso del modelo. Intenta más tarde.";
            }
            return "Error del servicio de IA: " + msg;
        }

        if (root instanceof com.google.genai.errors.ClientException _) {
            // 400, 404, etc.
            return "Error de configuración o petición inválida hacia el modelo de IA.";
        }

        // Fallback genérico
        return "Lo siento, ha ocurrido un error en el stream. Inténtalo nuevamente en unos momentos.";
    }

}
