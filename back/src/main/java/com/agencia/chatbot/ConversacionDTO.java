package com.agencia.chatbot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConversacionDTO(

        UUID id,

        String titulo,

        LocalDateTime fechaCreacion,

        LocalDateTime fechaActualizacion,

        List<MensajeChatDTO> mensajes

) {
}
