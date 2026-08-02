package com.agencia.chatbot;

import com.agencia.domain.Usuario;
import com.agencia.exception.ConversacionException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;
    private final ConversacionRepository conversacionRepository;
    private final GuardarChatService guardarChatService;
    private final ConversacionMapper conversacionMapper;


    @Override
    public SseEmitter chat(ChatRequest request, Usuario usuario) {

        log.info("Iniciando chat con usuario {}", usuario.getUsuario());

        Conversacion conversacion = obtenerOCrearConversacion(request, usuario);
        AtomicBoolean errorSistema = new AtomicBoolean(false);
        SseEmitter emitter = new SseEmitter(180_000L);

        // TODO modificar el front para que acepte el conversationId y lo envie en siguientes peticiones

        // Envíar id de la conversacion
        if (!enviarEvento(emitter, "conversationId", conversacion.getId())) {
            return emitter;
        }

        StringBuilder respuestaCompleta = new StringBuilder();

        Disposable disposable = chatClient.prompt()
                .user(request.peticion())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacion.getId().toString()))
                .stream()
                .content()
                .doOnNext(respuestaCompleta::append)
                .doFinally(_ -> {
                    if (!respuestaCompleta.isEmpty() && !errorSistema.get()) {
                        guardarConversacionAsync(conversacion, request.peticion(), respuestaCompleta.toString(), errorSistema.get());
                    }
                    log.info("Conversación {} finalizada", conversacion.getId());
                })
                .subscribe(
                        token -> enviarEvento(emitter, "message", token),
                        error -> {
                            log.error("Error durante el streaming", error);
                            errorSistema.set(true);
                            enviarEvento(emitter, "error", "Lo siento, ha ocurrido un error. Inténtalo nuevamente en unos momentos.");
                            emitter.complete();
                        },
                        emitter::complete
                );

        emitter.onCompletion(disposable::dispose);
        emitter.onTimeout(disposable::dispose);
        emitter.onError(_ -> disposable.dispose());


        return emitter;
    }

    @Override
    public ConversacionDTO getConversation(Long id, Usuario usuario) {
        Conversacion conversacion = conversacionRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ConversacionException("Conversación " + id + " no encontrada"));

        return conversacionMapper.toResponse(conversacion);
    }


    private Conversacion obtenerOCrearConversacion(ChatRequest request, Usuario usuario) {
        return conversacionRepository.conversacionExiste(request.conversacionId(), usuario.getId())
                .orElseGet(() -> {
                    LocalDateTime fecha = LocalDateTime.now();
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


    private void guardarConversacionAsync(Conversacion conversacion, String pregunta, String respuesta, boolean errorSistema) {

        Mono.fromRunnable(() -> guardarChatService.guardarRespuestas(respuesta, pregunta, conversacion, errorSistema))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        error -> log.error("Error guardando conversación {}", conversacion.getId(), error)
                );
    }

    private boolean enviarEvento(SseEmitter emitter, String event, Object data) {
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

}
