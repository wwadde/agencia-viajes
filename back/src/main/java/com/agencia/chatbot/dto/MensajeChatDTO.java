package com.agencia.chatbot.dto;

import com.agencia.chatbot.entity.RolMensaje;

import java.time.LocalDateTime;

public record MensajeChatDTO(

        Long id,

        String mensaje,

        RolMensaje rolMensaje,

        LocalDateTime fechaHora

) {
}
