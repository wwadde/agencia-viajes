package com.agencia.foro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ForoPostUpsertDTO(
        @NotBlank(message = "El titulo de la publicación es obligatorio")
        @Size(max = 180, message = "El titulo no puede superar 180 caracteres")
        String titulo,

        @Size(max = 240, message = "El slug no puede superar 240 caracteres")
        String slug,

        @NotBlank(message = "El resumen de la publicación es obligatorio")
        @Size(max = 2000, message = "El resumen no puede superar 2000 caracteres")
        String resumen,

        @NotNull(message = "Debe indicar si la publicación está activa")
        Boolean publicado,

        @Valid
        @NotNull(message = "Debe enviar al menos un bloque de contenido")
        @Size(min = 1, message = "Debe agregar al menos un bloque de contenido")
        List<ForoBloqueUpsertDTO> bloques
) {
}
