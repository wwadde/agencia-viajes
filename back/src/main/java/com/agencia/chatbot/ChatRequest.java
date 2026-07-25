package com.agencia.chatbot;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(

        @NotBlank(message = "Envía un mensaje para que el chatbot pueda responder.")
        String peticion,
        Long conversacionId

) {
}
