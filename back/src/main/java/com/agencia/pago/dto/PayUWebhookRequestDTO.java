package com.agencia.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayUWebhookRequestDTO(
        @NotBlank(message = "La referencia de venta de PayU es obligatoria")
        String referenceSale,

        @NotBlank(message = "El estado de PayU es obligatorio")
        String statePol,

        @NotBlank(message = "La firma de PayU es obligatoria")
        @Size(max = 512, message = "La firma no puede superar 512 caracteres")
        String sign,

        @NotBlank(message = "El valor de PayU es obligatorio")
        String value,

        @NotBlank(message = "La moneda de PayU es obligatoria")
        String currency,

        String transactionState,
        String transactionId,
        String responseMessagePol,
        @Size(max = 8000, message = "El payload no puede superar 8000 caracteres")
        String payload
) {
}

