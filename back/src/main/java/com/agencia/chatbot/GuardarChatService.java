package com.agencia.chatbot;

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
    public void guardarRespuestas(String respuesta, String peticion, Conversacion conversacion, boolean error) {

        LocalDateTime fecha = LocalDateTime.now();

        MensajeChat mensajeUsuario = MensajeChat.builder()
                .mensaje(peticion)
                .fechaHora(fecha)
                .rolMensaje(RolMensaje.USUARIO)
                .conversacion(conversacion)
                .build();

        MensajeChat mensajeSistema = MensajeChat.builder()
                .mensaje(respuesta)
                .fechaHora(fecha)
                .rolMensaje(error ? RolMensaje.SYSTEM : RolMensaje.CHATBOT)
                .conversacion(conversacion)
                .build();

        mensajeChatRepository.saveAll(List.of(
                mensajeUsuario,
                mensajeSistema
        ));

        conversacionRepository.actualizarFecha(conversacion.getId(), fecha);
    }


}
