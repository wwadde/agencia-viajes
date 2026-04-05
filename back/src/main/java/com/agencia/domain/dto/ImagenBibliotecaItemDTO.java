package com.agencia.domain.dto;

import java.time.Instant;

/**
 * Item de la biblioteca de imágenes del panel admin.
 */
public record ImagenBibliotecaItemDTO(
        String referencia,
        String url,
        String thumbnailReferencia,
        String thumbnailUrl,
        String carpeta,
        String nombre,
        long tamanoBytes,
        Instant fechaModificacion
) {
}

