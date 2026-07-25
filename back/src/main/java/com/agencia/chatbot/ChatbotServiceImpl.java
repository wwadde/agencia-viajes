package com.agencia.chatbot;

import com.agencia.domain.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;
    private final ConversacionRepository conversacionRepository;
    private final GuardarChatService guardarChatService;


    private static final String SYSTEM_PROMPT = """
            Actúa como un asistente de viajes que responde a las preguntas de los usuarios sobre destinos,
            paquetes turísticos y recomendaciones de viaje. Proporciona información útil y precisa, y sugiere
            opciones de viaje según las preferencias del usuario. Si te preguntan de otras cosas no relacionadas
            a los viajes, responde amablemente que estás especializado en viajes y no puedes ayudar con ese tema.
            Proporciona respuestas cortas
            """;


    public ChatbotServiceImpl(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, ConversacionRepository conversacionRepository, GuardarChatService guardarChatService) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.conversacionRepository = conversacionRepository;
        this.guardarChatService = guardarChatService;
    }

    @Override
    public SseEmitter chat(ChatRequest request, Usuario usuario) {

        log.info("Iniciando chat con usuario {}", usuario.getUsuario());

        Conversacion conversacion = obtenerOCrearConversacion(request, usuario);

        SseEmitter emitter = new SseEmitter(180_000L);

        StringBuilder respuestaCompleta = new StringBuilder();

        chatClient.prompt()
                .user(request.peticion())
//              .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversacion.getId().toString()))
                .stream()
                .content()
                .doOnNext(respuestaCompleta::append)
                .doOnComplete(() -> {
                    guardarConversacionAsync(
                            conversacion,
                            request.peticion(),
                            respuestaCompleta.toString()
                    );
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
                            emitter.completeWithError(error);
                        },
                        emitter::complete
                );

        return emitter;
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

        Mono.fromRunnable(() ->
                        guardarChatService.guardarRespuestas(
                                respuesta,
                                pregunta,
                                conversacion
                        ))
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
