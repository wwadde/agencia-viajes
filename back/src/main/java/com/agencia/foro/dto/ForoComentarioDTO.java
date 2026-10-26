package com.agencia.foro.dto;

import java.time.LocalDateTime;

public record ForoComentarioDTO(
        Long id,
        String autor,
        String contenido,
        LocalDateTime fechaCreacion,
        Boolean oculto,
        Long totalLikes,
        Boolean likedByMe
) {
}
