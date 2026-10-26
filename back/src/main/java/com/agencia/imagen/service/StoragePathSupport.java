package com.agencia.imagen.service;

import com.agencia.imagen.exception.ArchivoInvalidoException;
import com.agencia.imagen.exception.CarpetaInvalidaException;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class StoragePathSupport {

    private static final String WEBP_EXTENSION = ".webp";
    static final String MINIATURES_DIRECTORY = "miniaturas";
    private static final Pattern ILLEGAL_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    private static final Pattern SAFE_DIRECTORY = Pattern.compile("^[a-z0-9_-]+$");
    private static final Pattern DIMENSION_SUFFIX = Pattern.compile("_(\\d+)w$");

    private final Path rootLocation;
    private final Set<String> allowedDirectories;
    StoragePathSupport(String location, List<String> configuredDirectories) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        this.allowedDirectories = configuredDirectories.stream()
                .map(this::normalizeDirectoryToken)
                .collect(Collectors.toUnmodifiableSet());
    }

    Path rootLocation() {
        return rootLocation;
    }

    Set<String> allowedDirectories() {
        return allowedDirectories;
    }

    void validateRootAccess(Path resolvedPath, String carpetaObjetivo) {
        if (!resolvedPath.startsWith(rootLocation)) {
            throw new ArchivoInvalidoException("Carpeta no permitida: " + carpetaObjetivo);
        }
    }

    Path resolveSecureReference(String referencia) {
        String normalizedReference = normalizeReference(referencia);
        Path resolved = rootLocation.resolve(normalizedReference).normalize();
        if (!resolved.startsWith(rootLocation)) {
            throw new ArchivoInvalidoException("Referencia de archivo no permitida: " + referencia);
        }
        return resolved;
    }

    String normalizeReference(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new ArchivoInvalidoException("La referencia del archivo es obligatoria");
        }

        String normalized = referencia.replace('\\', '/').trim();
        if (normalized.startsWith("/") || normalized.contains("..") || !normalized.contains("/")) {
            throw new ArchivoInvalidoException("Referencia de archivo no permitida: " + referencia);
        }

        String[] segments = normalized.split("/");
        if (segments.length != 2) {
            throw new ArchivoInvalidoException("La referencia debe tener formato carpeta/archivo");
        }

        String directory = validateAndNormalizeDirectory(segments[0]);
        String filename = sanitizeFilename(segments[1]);
        if (filename.isBlank()) {
            throw new ArchivoInvalidoException("Nombre de archivo no permitido");
        }

        return directory + "/" + filename;
    }

    String validateAndNormalizeDirectory(String carpeta) {
        String normalized = normalizeDirectoryToken(carpeta);
        if (!allowedDirectories.contains(normalized)) {
            throw new CarpetaInvalidaException("Carpeta no permitida: " + carpeta);
        }
        return normalized;
    }

    String validateAndNormalizeUploadDirectory(String carpeta) {
        String normalized = validateAndNormalizeDirectory(carpeta);
        if (MINIATURES_DIRECTORY.equals(normalized)) {
            throw new CarpetaInvalidaException("Carpeta no permitida para carga directa: " + carpeta);
        }
        return normalized;
    }

    String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ArchivoInvalidoException("El archivo no tiene nombre válido");
        }

        String normalized = StringUtils.cleanPath(filename).trim().replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }

        if (normalized.isBlank() || normalized.equals(".") || normalized.equals("..")) {
            throw new ArchivoInvalidoException("El archivo no tiene nombre válido");
        }

        if (ILLEGAL_FILENAME_CHARS.matcher(normalized).find()) {
            throw new ArchivoInvalidoException("El nombre del archivo contiene caracteres no permitidos");
        }

        return normalized;
    }

    String buildOptimizedFilename(String originalName) {
        return buildBaseName(originalName) + WEBP_EXTENSION;
    }

    String buildThumbnailFilename(String originalName, int width) {
        return buildBaseName(originalName) + "_" + width + "w" + WEBP_EXTENSION;
    }

    String toThumbnailReference(String normalizedReference, int width) {
        String[] parts = normalizedReference.split("/");
        String fileName = parts[1];
        String baseName = StringUtils.stripFilenameExtension(fileName);
        return MINIATURES_DIRECTORY + "/" + baseName + "_" + width + "w" + WEBP_EXTENSION;
    }

    Set<String> toOriginalReferencesFromThumbnail(String normalizedReference) {
        String[] parts = normalizedReference.split("/");
        if (parts.length != 2 || !MINIATURES_DIRECTORY.equals(parts[0])) {
            return Set.of();
        }

        String fileName = parts[1];
        String baseNameWithSuffix = StringUtils.stripFilenameExtension(fileName);
        java.util.regex.Matcher matcher = DIMENSION_SUFFIX.matcher(baseNameWithSuffix);
        if (!matcher.find()) {
            return Set.of();
        }

        String baseName = baseNameWithSuffix.substring(0, matcher.start());
        return allowedDirectories.stream()
                .filter(directory -> !MINIATURES_DIRECTORY.equals(directory))
                .map(directory -> directory + "/" + baseName + WEBP_EXTENSION)
                .collect(Collectors.toSet());
    }

    boolean isGeneratedThumbnail(String carpeta, String fileName) {
        if (MINIATURES_DIRECTORY.equals(carpeta)) {
            return true;
        }

        String baseName = StringUtils.stripFilenameExtension(fileName);
        return DIMENSION_SUFFIX.matcher(baseName).find();
    }

    private String normalizeDirectoryToken(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ArchivoInvalidoException("La carpeta destino es obligatoria");
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!SAFE_DIRECTORY.matcher(normalized).matches()) {
            throw new CarpetaInvalidaException("Carpeta no permitida: " + raw);
        }
        return normalized;
    }

    private String buildBaseName(String originalName) {
        String sanitized = sanitizeFilename(originalName);
        String baseName = StringUtils.stripFilenameExtension(sanitized);
        if (baseName.isBlank()) {
            baseName = "imagen";
        }
        return baseName;
    }
}


