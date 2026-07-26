package com.agencia.chatbot;

import java.time.LocalDateTime;
import java.util.List;

public record ConversacionDTO(

        Long id,

        String titulo,

        LocalDateTime fechaCreacion,

        LocalDateTime fechaActualizacion,

        List<MensajeChatDTO> mensajes

) {
}
