package com.agencia.pago.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PagoCrearIntentoDTO(
        @Pattern(regexp = "^[A-Z]{3}$", message = "La moneda debe tener formato ISO-4217 de 3 letras")
        String moneda,

        @Size(max = 120, message = "La llave de idempotencia no puede superar 120 caracteres")
        String idempotencyKey
) {
}

