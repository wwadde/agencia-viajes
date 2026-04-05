package com.agencia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propiedades de configuración para el almacenamiento de archivos.
 *
 * <p>Se vinculan desde el prefijo {@code storage} en los archivos de configuración.
 * Ejemplo en application.yaml:
 * <pre>
 * storage:
 *   location: /app/images
 *   max-file-size-bytes: 5242880
 * </pre>
 */
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        String location,
        long maxFileSizeBytes,
        List<String> allowedDirectories,
        int webpQuality,
        int thumbnailWidth,
        int thumbnailHeight,
        String thumbnailSuffix
) {
    public StorageProperties {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("storage.location no puede estar vacío");
        }
        if (maxFileSizeBytes <= 0) {
            maxFileSizeBytes = 4 * 1024 * 1024L; // 4 MB por defecto
        }
        if (allowedDirectories == null || allowedDirectories.isEmpty()) {
            allowedDirectories = List.of("paquetes", "iconos", "landing");
        }
        if (webpQuality < 1 || webpQuality > 100) {
            webpQuality = 82;
        }
        if (thumbnailWidth <= 0) {
            thumbnailWidth = 480;
        }
        if (thumbnailHeight <= 0) {
            thumbnailHeight = 320;
        }
        if (thumbnailSuffix == null || thumbnailSuffix.isBlank()) {
            thumbnailSuffix = "_thumb";
        }
    }
}

