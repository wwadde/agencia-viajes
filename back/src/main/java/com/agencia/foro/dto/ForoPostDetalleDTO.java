package com.agencia.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ForoPostDetalleDTO(
        Long id,
        String titulo,
        String slug,
        String resumen,
        Boolean publicado,
        LocalDateTime fechaPublicacion,
        LocalDateTime ultimaModificacion,
        String autor,
        Long totalLikes,
        Boolean likedByMe,
        List<ForoBloqueDTO> bloques,
        List<ForoComentarioDTO> comentarios
) {
}
