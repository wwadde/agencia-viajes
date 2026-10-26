package com.agencia.foro.dto;

import java.time.LocalDateTime;

public record ForoPostResumenDTO(
        Long id,
        String titulo,
        String slug,
        String resumen,
        String imagenPortada,
        LocalDateTime fechaPublicacion,
        Long totalComentarios,
        Long totalLikes,
        Boolean likedByMe
) {
}
