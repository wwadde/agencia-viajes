package com.agencia.imagen.service;

import java.time.Instant;

/**
 * Metadatos de un archivo almacenado para uso en biblioteca admin.
 */
public record StorageFileInfo(
        String referencia,
        String carpeta,
        String nombre,
        long tamanoBytes,
        Instant fechaModificacion
) {
}

