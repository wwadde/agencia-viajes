package com.agencia.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgendaSalidaUpsertDTO(
        @NotNull(message = "La fecha de salida es obligatoria")
        @FutureOrPresent(message = "La fecha de salida debe ser hoy o futura")
        LocalDate fechaSalida,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        BigDecimal precio,

        @NotNull(message = "Los cupos disponibles son obligatorios")
        @PositiveOrZero(message = "Los cupos disponibles no pueden ser negativos")
        Integer cuposDisponibles,

        @NotNull(message = "El paquete es obligatorio")
        Long paqueteId
) {
}

