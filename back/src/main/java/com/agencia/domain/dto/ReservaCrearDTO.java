package com.agencia.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReservaCrearDTO(
        @NotNull(message = "La salida es obligatoria")
        Long salidaId,

        @NotNull(message = "La cantidad de personas es obligatoria")
        @Positive(message = "La cantidad de personas debe ser mayor a 0")
        Integer cantidadPersonas,

        @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
        String observaciones
) {
}

