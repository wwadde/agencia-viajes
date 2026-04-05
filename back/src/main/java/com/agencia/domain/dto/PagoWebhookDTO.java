package com.agencia.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PagoWebhookDTO(
        @NotBlank(message = "La referencia externa es obligatoria")
        String referenciaExterna,

        @NotBlank(message = "El estado del pago de la pasarela es obligatorio")
        String estadoPasarela,

        @Size(max = 512, message = "La firma no puede superar 512 caracteres")
        String firma,

        @Size(max = 8000, message = "El payload no puede superar 8000 caracteres")
        String payload
) {
}

