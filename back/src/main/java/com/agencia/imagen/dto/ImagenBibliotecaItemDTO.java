package com.agencia.imagen.dto;

import java.time.Instant;

/**
 * Item de la biblioteca de imágenes del panel admin.
 */
public record ImagenBibliotecaItemDTO(
        String referencia,
        String url,
        String thumbnailReferencia,
        String thumbnailUrl,
        String thumbnailSmallReferencia,
        String thumbnailSmallUrl,
        String thumbnailMediumReferencia,
        String thumbnailMediumUrl,
        String carpeta,
        String nombre,
        long tamanoBytes,
        Instant fechaModificacion
) {
}

