package com.agencia.domain.dto;

import com.agencia.domain.ReservaEstado;
import jakarta.validation.constraints.NotNull;

public record ReservaEstadoUpdateDTO(
        @NotNull(message = "El estado es obligatorio")
        ReservaEstado estado
) {
}

