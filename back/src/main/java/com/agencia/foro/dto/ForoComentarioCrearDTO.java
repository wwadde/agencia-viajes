package com.agencia.foro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForoComentarioCrearDTO(
        @NotBlank(message = "El comentario no puede estar vacío")
        @Size(max = 1500, message = "El comentario no puede superar 1500 caracteres")
        String contenido
) {
}
