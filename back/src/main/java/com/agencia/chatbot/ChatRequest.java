package com.agencia.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(

        @NotBlank(message = "Envía un mensaje para que el chatbot pueda responder.")
        @Size(max = 250, message = "El mensaje no puede superar los 250 caracteres.")
        String peticion,

        UUID conversacionId

) {
}
