package com.agencia.reserva.dto;

import com.agencia.reserva.entity.ReservaEstado;
import jakarta.validation.constraints.NotNull;

public record ReservaEstadoUpdateDTO(
        @NotNull(message = "El estado es obligatorio")
        ReservaEstado estado
) {
}

