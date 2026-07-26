package com.agencia.chatbot;

import java.time.LocalDateTime;

public record MensajeChatDTO(

        Long id,

        String mensaje,

        RolMensaje rolMensaje,

        LocalDateTime fechaHora

) {
}
