package com.agencia.chatbot;

import com.agencia.domain.Usuario;
import com.agencia.exception.ConversacionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;
    private final ConversacionRepository conversacionRepository;
    private final GuardarChatService guardarChatService;
    private final ConversacionMapper conversacionMapper;


    private static final String SYSTEM_PROMPT = """
            Eres el asistente virtual de una agencia de viajes.
            
            Tu función es ayudar exclusivamente con temas relacionados con viajes y turismo.
            
            Puedes ayudar con:
            - Destinos turísticos.
            - Recomendaciones de lugares para visitar.
            - Paquetes turísticos.
            - Hoteles y alojamiento.
            - Transporte y vuelos.
            - Itinerarios de viaje.
            - Requisitos de viaje cuando sean conocidos.
            - Clima, temporadas y actividades turísticas.
            - Consejos para viajeros.
            
            Si una pregunta no está relacionada con viajes o turismo, responde amablemente que eres un
            asistente especializado en viajes y no puedes ayudar con ese tema.
            
            No inventes información. Si no conoces un dato o no estás seguro, indícalo claramente.
            
            Responde de forma clara, amable y concisa. Utiliza listas cuando mejoren la legibilidad.
            No finalices todas las respuestas con una pregunta. Hazlo únicamente cuando aporte valor para continuar la conversación.
            Responde de forma amable, clara y concia. Sé breve en las respuestas.
            
            Cuando sea útil, utiliza listas simples iniciadas con "-".
            No utilices Markdown.
            No uses negritas, cursivas, encabezados, tablas ni bloques de código.
            Utiliza únicamente texto plano y saltos de línea.
            """;


    public ChatbotServiceImpl(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                              ConversacionRepository conversacionRepository,
                              GuardarChatService guardarChatService,
                              ConversacionMapper conversacionMapper) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.conversacionRepository = conversacionRepository;
        this.guardarChatService = guardarChatService;
        this.conversacionMapper = conversacionMapper;
    }

    @Override
    public SseEmitter chat(ChatRequest request, Usuario usuario) {

        log.info("Iniciando chat con usuario {}", usuario.getUsuario());

        // TODO Implementar contexto de app

        Conversacion conversacion = obtenerOCrearConversacion(request, usuario);
        AtomicBoolean errorSistema = new AtomicBoolean(false);

        SseEmitter emitter = new SseEmitter(180_000L);

        try {

            emitter.send(
                    SseEmitter.event()
                            .name("conversationId")
                            .data(conversacion.getId())
            );
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        StringBuilder respuestaCompleta = new StringBuilder();

        Disposable disposable = chatClient.prompt()
                .user(request.peticion())
//              .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacion.getId().toString()))
                .stream()
                .content()
                .doOnNext(respuestaCompleta::append)
                .doFinally(_ -> {
                    if (!respuestaCompleta.isEmpty()) {
                        guardarConversacionAsync(
                                conversacion,
                                request.peticion(),
                                respuestaCompleta.toString()
                        );
                    }
                    log.info("Conversación {} finalizada", conversacion.getId());
                })
                .subscribe(
                        token -> {
                            try {
                                emitter.send(
                                        SseEmitter.event()
                                                .name("message")
                                                .data(token)
                                );
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("Error durante el streaming", error);
                            errorSistema.set(true);

                            try {
                                emitter.send(
                                        SseEmitter.event()
                                                .name("error")
                                                .data("Lo siento, ha ocurrido un error. Inténtalo nuevamente en unos momentos.")
                                );
                            } catch (IOException _) {
                                emitter.completeWithError(error);
                            }

                            emitter.complete();
                        },
                        emitter::complete
                );

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(disposable::dispose);
        emitter.onError(_ -> disposable.dispose());

        if (errorSistema.get()) {
            guardarChatService.guardarRespuestas(respuestaCompleta.toString(), request.peticion(), conversacion, true);
        }


        return emitter;
    }

    @Override
    public ConversacionDTO getConversation(Long id, Usuario usuario) {
        Conversacion conversacion = conversacionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ConversacionException("Conversación " + id + " no encontrada"));

        // TODO verificar si el formato de las fechas es correcto, cual se usa generalmente UTC, GMT-5 etc...
        return conversacionMapper.toResponse(conversacion);
    }


    private Conversacion obtenerOCrearConversacion(ChatRequest request, Usuario usuario) {
        return conversacionRepository
                .conversacionExiste(request.conversacionId(), usuario.getId())
                .orElseGet(() ->
                        conversacionRepository.save(this.crearConversacion(request.peticion(), usuario)));
    }

    private Conversacion crearConversacion(String peticion, Usuario usuario) {
        LocalDateTime fecha = LocalDateTime.now();
        return Conversacion.builder()
                .titulo(StringUtils.abbreviate(peticion, 60)
                        .replace("\n", " ")
                        .trim())
                .usuario(usuario)
                .fechaCreacion(fecha)
                .fechaActualizacion(fecha)
                .build();
    }

    private void guardarConversacionAsync(
            Conversacion conversacion,
            String pregunta,
            String respuesta) {

        Mono.fromRunnable(() -> guardarChatService.guardarRespuestas(respuesta, pregunta, conversacion, false))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> log.error(
                                "Error guardando conversación {}",
                                conversacion.getId(),
                                error
                        )
                );
    }

}
