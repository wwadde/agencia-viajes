package com.agencia.service;

import com.agencia.exception.AlmacenamientoException;
import com.agencia.exception.ArchivoInvalidoException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

final class StorageImageValidator {

    private static final Map<String, byte[]> ALLOWED_SIGNATURES = Map.of(
            "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private static final byte[] WEBP_RIFF = new byte[]{0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_WEBP = new byte[]{0x57, 0x45, 0x42, 0x50};

    private final long maxFileSizeBytes;

    StorageImageValidator(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSizeBytes) {
            throw new ArchivoInvalidoException(
                    "El archivo supera el tamaño máximo permitido de %d bytes".formatted(maxFileSizeBytes));
        }
    }

    String validateAndGetContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ArchivoInvalidoException(
                    "Tipo de archivo no permitido. Se aceptan: " + ALLOWED_MIME_TYPES);
        }
        return contentType;
    }

    void validateMagicBytes(MultipartFile file, String contentType) {
        if ("image/webp".equals(contentType)) {
            validateWebpMagicBytes(file);
            return;
        }

        byte[] signature = ALLOWED_SIGNATURES.get(contentType);
        validateSignaturePrefix(file, contentType, signature);
    }

    private void validateSignaturePrefix(MultipartFile file, String contentType, byte[] signature) {
        byte[] header = readHeader(file, signature.length);
        for (int i = 0; i < signature.length; i++) {
            if (header[i] != signature[i]) {
                throw new ArchivoInvalidoException(
                        "El contenido del archivo no corresponde al tipo declarado: " + contentType);
            }
        }
    }

    private void validateWebpMagicBytes(MultipartFile file) {
        byte[] header = readHeader(file, 12);

        for (int i = 0; i < WEBP_RIFF.length; i++) {
            if (header[i] != WEBP_RIFF[i]) {
                throw new ArchivoInvalidoException("El contenido del archivo no corresponde al tipo declarado: image/webp");
            }
        }

        for (int i = 0; i < WEBP_WEBP.length; i++) {
            if (header[8 + i] != WEBP_WEBP[i]) {
                throw new ArchivoInvalidoException("El contenido del archivo no corresponde al tipo declarado: image/webp");
            }
        }
    }

    private byte[] readHeader(MultipartFile file, int length) {
        byte[] header = new byte[length];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header, 0, header.length);
            if (bytesRead < length) {
                throw new ArchivoInvalidoException("El archivo es demasiado pequeño para ser una imagen válida");
            }
            return header;
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo leer el archivo para validarlo", e);
        }
    }
}

