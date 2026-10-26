package com.agencia.paquete.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ImagenPaqueteUpsertDTO(

        @NotBlank(message = "La referencia de la imagen es obligatoria")
        @Size(max = 2048, message = "La referencia de la imagen no puede superar 2048 caracteres")
        String referencia,

        @NotNull(message = "El orden de la imagen es obligatorio")
        @PositiveOrZero(message = "El orden de la imagen no puede ser negativo")
        Integer orden,

        Boolean principal
) {
}

