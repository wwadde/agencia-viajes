package com.agencia.foro.dto;

import com.agencia.foro.entity.ForoBloqueTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ForoBloqueUpsertDTO(
        @NotNull(message = "El tipo de bloque es obligatorio")
        ForoBloqueTipo tipo,

        @NotBlank(message = "El contenido del bloque es obligatorio")
        @Size(max = 5000, message = "El contenido del bloque no puede superar 5000 caracteres")
        String contenido,

        @Size(max = 240, message = "El pie de imagen no puede superar 240 caracteres")
        String pieImagen,

        @NotNull(message = "El orden del bloque es obligatorio")
        @PositiveOrZero(message = "El orden del bloque no puede ser negativo")
        Integer orden
) {
}
