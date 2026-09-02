package com.agencia.chatbot.service;

import com.agencia.chatbot.entity.Conversacion;
import com.agencia.chatbot.repository.ConversacionRepository;
import com.agencia.chatbot.repository.MensajeChatRepository;
import com.agencia.chatbot.entity.MensajeChat;
import com.agencia.chatbot.entity.RolMensaje;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardarChatService {

    private final ConversacionRepository conversacionRepository;
    private final MensajeChatRepository mensajeChatRepository;

    @Transactional
    public void guardarRespuestas(String respuesta, String peticion, Conversacion conversacion, boolean error,
                                  Integer inputTokens, Integer outputTokens, Integer totalTokens) {

        LocalDateTime fecha = LocalDateTime.now();

        MensajeChat mensajeUsuario = MensajeChat.builder()
                .mensaje(peticion)
                .fechaHora(fecha)
                .rolMensaje(RolMensaje.USUARIO)
                .conversacion(conversacion)
                .inputTokens(inputTokens)
                .build();

        MensajeChat mensajeSistema = MensajeChat.builder()
                .mensaje(respuesta)
                .fechaHora(fecha)
                .rolMensaje(error ? RolMensaje.SYSTEM : RolMensaje.CHATBOT)
                .conversacion(conversacion)
                .outputTokens(outputTokens)
                .totalTokens(totalTokens)
                .build();

        mensajeChatRepository.saveAll(List.of(
                mensajeUsuario,
                mensajeSistema
        ));

        conversacionRepository.actualizarMetadata(conversacion.getId(), fecha, conversacion.getMetadata());
    }


}
