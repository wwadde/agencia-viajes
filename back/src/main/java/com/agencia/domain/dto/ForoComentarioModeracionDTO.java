package com.agencia.domain.dto;

import jakarta.validation.constraints.NotNull;

public record ForoComentarioModeracionDTO(
        @NotNull(message = "Debe indicar si el comentario estará oculto")
        Boolean oculto
) {
}
