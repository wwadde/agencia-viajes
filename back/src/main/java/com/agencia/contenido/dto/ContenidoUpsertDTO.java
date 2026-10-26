package com.agencia.contenido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContenidoUpsertDTO(
        @NotBlank(message = "La clave del contenido es obligatoria")
        @Size(max = 120, message = "La clave del contenido no puede superar 120 caracteres")
        String clave,

        @Size(max = 5000, message = "El valor del contenido no puede superar 5000 caracteres")
        String valor
) {
}

