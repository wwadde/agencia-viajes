package com.agencia.service;

import com.agencia.config.StorageProperties;
import com.agencia.exception.AlmacenamientoException;
import com.agencia.exception.ArchivoInvalidoException;
import com.agencia.exception.ArchivoNoEncontradoException;
import com.agencia.exception.CarpetaInvalidaException;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.ScaleMethod;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implementación de {@link StorageService} que persiste los archivos
 * en el sistema de archivos local del contenedor.
 *
 * <p>El directorio raíz se configura mediante la propiedad {@code storage.location}
 * y es compartido con Nginx a través de un volumen Docker para servir las
 * imágenes como contenido estático.
 *
 * <p>Validaciones aplicadas:
 * <ul>
 *   <li>El archivo no puede estar vacío.</li>
 *   <li>El {@code Content-Type} debe pertenecer a los tipos MIME permitidos.</li>
 *   <li>Los magic bytes del archivo deben coincidir con el tipo declarado
 *       (protección contra spoofing de extensión).</li>
 *   <li>El tamaño no puede superar {@code storage.max-file-size-bytes}.</li>
 *   <li>El nombre resuelto debe permanecer dentro del directorio raíz
 *       (protección contra path traversal).</li>
 * </ul>
 */


// TODO hacer varias miniaturas de la misma imagen para que desde angular pueda cargar una u otra dependienod el viewport, srcimg o algo asi con picture
    // TODO y seguir verificando la compresion de las imagenes
    // TODO La clase se volvio muy larga entonces se podria separar o por lo menos organizarla para que no hayan tantos metodos dentro de metodos privados por ejemplo, mas dificil de hacer debug si el metodo privado en general no supera los 100 lineas


@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    /** Tipos MIME aceptados y sus respectivos magic bytes (firma de archivo). */
    private static final Map<String, byte[]> ALLOWED_SIGNATURES = Map.of(
            "image/jpeg",  new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png",   new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
    );

    /** Extensiones canónicas para cada tipo MIME aceptado. */
    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private static final byte[] WEBP_RIFF = new byte[]{0x52, 0x49, 0x46, 0x46}; // "RIFF"
    private static final byte[] WEBP_WEBP = new byte[]{0x57, 0x45, 0x42, 0x50}; // "WEBP"
    private static final String WEBP_EXTENSION = ".webp";
    private static final int WEBP_MIN_QUALITY = 45;
    private static final int WEBP_QUALITY_STEP = 8;
    private static final int WEBP_METHOD = 6;
    private static final int WEBP_DEFLATE_LEVEL = 9;
    private static final int THUMBNAIL_MAX_QUALITY = 76;

    private static final Set<String> ALLOWED_MIME_TYPES = MIME_TO_EXTENSION.keySet();
    private static final Pattern ILLEGAL_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");
    private static final Pattern SAFE_DIRECTORY = Pattern.compile("^[a-z0-9_-]+$");

    private final Path rootLocation;
    private final long maxFileSizeBytes;
    private final Set<String> allowedDirectories;
    private final int webpQuality;
    private final int thumbnailWidth;
    private final int thumbnailHeight;
    private final String thumbnailSuffix;

    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.location()).toAbsolutePath().normalize();
        this.maxFileSizeBytes = properties.maxFileSizeBytes();
        this.allowedDirectories = properties.allowedDirectories().stream()
                .map(this::normalizeDirectoryToken)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        this.webpQuality = properties.webpQuality();
        this.thumbnailWidth = properties.thumbnailWidth();
        this.thumbnailHeight = properties.thumbnailHeight();
        this.thumbnailSuffix = properties.thumbnailSuffix();
        initDirectory();
    }


    @Override
    public String guardar(MultipartFile file, String carpeta) {
        if (file == null || file.isEmpty()) {
            throw new ArchivoInvalidoException("El archivo no puede estar vacío");
        }

        String nombreArchivo = file.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new ArchivoInvalidoException("El archivo no tiene nombre válido");
        }


        validateSize(file);
        String contentType = validateAndGetContentType(file);
        validateMagicBytes(file, contentType);

        String directory = validateAndNormalizeDirectory(carpeta);
        String filename = buildOptimizedFilename(nombreArchivo);
        String thumbnailFilename = buildThumbnailFilename(nombreArchivo);
        String referencia = directory + "/" + filename;
        String thumbnailReferencia = directory + "/" + thumbnailFilename;
        Path destination = resolveSecureReference(referencia);
        Path thumbnailDestination = resolveSecureReference(thumbnailReferencia);

        writeOptimizedFiles(
                file,
                destination,
                thumbnailDestination,
                referencia,
                "image/webp".equals(contentType)
        );
        log.info("Imagen guardada: {}", referencia);
        return referencia;
    }


    @Override
    public void eliminar(String referencia) {
        String normalizedReference = normalizeReference(referencia);
        Path target = resolveSecureReference(normalizedReference);
        if (!Files.exists(target)) {
            throw new ArchivoNoEncontradoException(referencia);
        }
        try {
            Files.delete(target);
            deleteGeneratedPairIfExists(normalizedReference);
            log.info("Imagen eliminada: {}", normalizedReference);
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo eliminar el archivo: " + normalizedReference, e);
        }
    }

    @Override
    public boolean existe(String referencia) {
        return Files.exists(resolveSecureReference(referencia));
    }

    @Override
    public List<StorageFileInfo> listar(String carpeta, String busqueda) {
        String query = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);

        Set<String> carpetasObjetivo = (carpeta == null || carpeta.isBlank())
                ? allowedDirectories
                : Set.of(validateAndNormalizeDirectory(carpeta));

        List<StorageFileInfo> items = new ArrayList<>();
        for (String carpetaObjetivo : carpetasObjetivo) {
            Path directoryPath = rootLocation.resolve(carpetaObjetivo).normalize();
            if (!directoryPath.startsWith(rootLocation)) {
                throw new ArchivoInvalidoException("Carpeta no permitida: " + carpetaObjetivo);
            }
            if (!Files.exists(directoryPath)) {
                continue;
            }

            try (Stream<Path> stream = Files.list(directoryPath)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> appendFileInfo(path, carpetaObjetivo, query, items));
            } catch (IOException e) {
                throw new AlmacenamientoException("No se pudo listar la carpeta: " + carpetaObjetivo, e);
            }
        }

        return items.stream()
                .sorted(Comparator
                        .comparing(StorageFileInfo::fechaModificacion, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(StorageFileInfo::referencia))
                .toList();
    }

    /**
     * Inicializa el directorio raíz de almacenamiento al arrancar la aplicación.
     */
    private void initDirectory() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Directorio de almacenamiento: {}", rootLocation);
        } catch (IOException e) {
            throw new AlmacenamientoException(
                    "No se pudo crear el directorio de almacenamiento: " + rootLocation, e);
        }
    }

    /**
     * Resuelve el nombre de archivo dentro del directorio raíz y valida
     * que la ruta resultante no escape de él (prevención de path traversal).
     */
    private Path resolveSecureReference(String referencia) {
        String normalizedReference = normalizeReference(referencia);
        Path resolved = rootLocation.resolve(normalizedReference).normalize();
        if (!resolved.startsWith(rootLocation)) {
            throw new ArchivoInvalidoException("Referencia de archivo no permitida: " + referencia);
        }
        return resolved;
    }

    private String normalizeReference(String referencia) {
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

    private String validateAndNormalizeDirectory(String carpeta) {
        String normalized = normalizeDirectoryToken(carpeta);
        if (!allowedDirectories.contains(normalized)) {
            throw new CarpetaInvalidaException("Carpeta no permitida: " + carpeta);
        }
        return normalized;
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

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSizeBytes) {
            throw new ArchivoInvalidoException(
                    "El archivo supera el tamaño máximo permitido de %d bytes".formatted(maxFileSizeBytes));
        }
    }

    /**
     * Valida el {@code Content-Type} declarado por el cliente y devuelve el valor normalizado.
     */
    private String validateAndGetContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ArchivoInvalidoException(
                    "Tipo de archivo no permitido. Se aceptan: " + ALLOWED_MIME_TYPES);
        }
        return contentType;
    }

    /**
     * Lee los primeros bytes del archivo y los compara con la firma conocida
     * para el tipo MIME declarado, evitando ataques de spoofing de extensión.
     */
    private void validateMagicBytes(MultipartFile file, String contentType) {
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

    public String sanitizeFilename(String filename) {
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

    private String buildBaseName(String originalName) {
        String sanitized = sanitizeFilename(originalName);
        String baseName = StringUtils.stripFilenameExtension(sanitized);
        if (baseName.isBlank()) {
            baseName = "imagen";
        }
        return baseName;
    }

    private String buildOptimizedFilename(String originalName) {
        return buildBaseName(originalName) + WEBP_EXTENSION;
    }

    private String buildThumbnailFilename(String originalName) {
        return buildBaseName(originalName) + thumbnailSuffix + WEBP_EXTENSION;
    }

    private String toThumbnailReference(String normalizedReference) {
        String[] parts = normalizedReference.split("/");
        String fileName = parts[1];
        String baseName = StringUtils.stripFilenameExtension(fileName);
        return parts[0] + "/" + baseName + thumbnailSuffix + WEBP_EXTENSION;
    }

    private String toOriginalReferenceFromThumbnail(String normalizedReference) {
        String[] parts = normalizedReference.split("/");
        String fileName = parts[1];
        String thumbnailToken = thumbnailSuffix + WEBP_EXTENSION;
        if (!fileName.endsWith(thumbnailToken)) {
            return null;
        }
        String baseName = fileName.substring(0, fileName.length() - thumbnailToken.length());
        return parts[0] + "/" + baseName + WEBP_EXTENSION;
    }

    private void deleteGeneratedPairIfExists(String normalizedReference) throws IOException {
        String thumbnailReference = toThumbnailReference(normalizedReference);
        if (!thumbnailReference.equals(normalizedReference)) {
            Files.deleteIfExists(resolveSecureReference(thumbnailReference));
        }

        String originalReference = toOriginalReferenceFromThumbnail(normalizedReference);
        if (originalReference != null && !originalReference.equals(normalizedReference)) {
            Files.deleteIfExists(resolveSecureReference(originalReference));
        }
    }

    private boolean isGeneratedThumbnail(String fileName) {
        return fileName.toLowerCase(Locale.ROOT)
                .endsWith((thumbnailSuffix + WEBP_EXTENSION).toLowerCase(Locale.ROOT));
    }

    private void appendFileInfo(Path path, String carpeta, String query, List<StorageFileInfo> items) {
        String fileName = path.getFileName().toString();

        // Si hay archivos legacy no válidos en disco, se omiten del listado admin.
        String safeName;
        try {
            safeName = sanitizeFilename(fileName);
        } catch (ArchivoInvalidoException ignored) {
            log.warn("Archivo omitido de biblioteca por nombre inválido: {}", fileName);
            return;
        }

        if (!safeName.equals(fileName)) {
            return;
        }

        if (isGeneratedThumbnail(fileName)) {
            return;
        }

        if (!query.isBlank() && !fileName.toLowerCase(Locale.ROOT).contains(query)) {
            return;
        }

        try {
            long tamano = Files.size(path);
            Instant fechaModificacion = Files.getLastModifiedTime(path).toInstant();
            String referencia = carpeta + "/" + safeName;
            items.add(new StorageFileInfo(referencia, carpeta, safeName, tamano, fechaModificacion));
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo leer metadatos del archivo: " + fileName, e);
        }
    }

    private void writeOptimizedFiles(
            MultipartFile file,
            Path destination,
            Path thumbnailDestination,
            String referencia,
            boolean sourceAlreadyWebp
    ) {
        try {
            Files.createDirectories(destination.getParent());
            byte[] originalBytes = file.getBytes();
            ImmutableImage image = ImmutableImage.loader().fromBytes(originalBytes);

            byte[] optimizedMain = optimizeMainImage(image, originalBytes, sourceAlreadyWebp);
            Files.write(destination, optimizedMain);

            ImmutableImage thumbnail = image.max(thumbnailWidth, thumbnailHeight, ScaleMethod.Lanczos3);
            byte[] optimizedThumbnail = optimizeThumbnail(thumbnail);
            Files.write(thumbnailDestination, optimizedThumbnail);

            log.info(
                    "Optimización {}: original={} bytes, principal={} bytes, thumbnail={} bytes",
                    referencia,
                    originalBytes.length,
                    optimizedMain.length,
                    optimizedThumbnail.length
            );
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoException("No se pudo guardar el archivo: " + referencia, e);
        }
    }

    private byte[] optimizeMainImage(ImmutableImage image, byte[] originalBytes, boolean sourceAlreadyWebp) {
        byte[] optimized = optimizeForTargetSize(image, originalBytes.length);

        if (sourceAlreadyWebp && originalBytes.length <= optimized.length) {
            log.info(
                    "Se conserva WebP original porque re-encodear no mejora tamaño (original={} bytes, reencode={} bytes)",
                    originalBytes.length,
                    optimized.length
            );
            return originalBytes;
        }

        return optimized;
    }

    private byte[] optimizeForTargetSize(ImmutableImage image, int maxTargetBytes) {
        int startQuality = Math.max(normalizeQuality(webpQuality), WEBP_MIN_QUALITY);
        byte[] best = null;
        int bestQuality = startQuality;

        for (int quality = startQuality; quality >= WEBP_MIN_QUALITY; quality -= WEBP_QUALITY_STEP) {
            byte[] candidate = encodeWebp(image, quality);
            if (best == null || candidate.length < best.length) {
                best = candidate;
                bestQuality = quality;
            }

            if (candidate.length <= maxTargetBytes) {
                log.debug("WebP optimizado con q={} ({} bytes)", quality, candidate.length);
                return candidate;
            }
        }

        log.warn(
                "No se logró reducir por debajo del original (original={} bytes, mejor={} bytes, q={})",
                maxTargetBytes,
                best == null ? -1 : best.length,
                bestQuality
        );
        return best;
    }

    private byte[] optimizeThumbnail(ImmutableImage image) {
        int startQuality = Math.max(Math.min(normalizeQuality(webpQuality), THUMBNAIL_MAX_QUALITY), WEBP_MIN_QUALITY);
        byte[] best = null;
        for (int quality = startQuality;
             quality >= WEBP_MIN_QUALITY;
             quality -= WEBP_QUALITY_STEP) {
            byte[] candidate = encodeWebp(image, quality);
            if (best == null || candidate.length < best.length) {
                best = candidate;
            }
        }
        return best;
    }

    private byte[] encodeWebp(ImmutableImage image, int quality) {
        WebpWriter writer = buildWriter(image, quality);
        try {
            return image.forWriter(writer).bytes();
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo codificar la imagen a WebP", e);
        }
    }

    private WebpWriter buildWriter(ImmutableImage image, int quality) {
        WebpWriter writer = WebpWriter.MAX_LOSSLESS_COMPRESSION
                .withQ(normalizeQuality(quality))
                .withM(WEBP_METHOD)
                .withZ(WEBP_DEFLATE_LEVEL);

        if (!image.awt().getColorModel().hasAlpha()) {
            writer = writer.withoutAlpha();
        }

        return writer;
    }

    private int normalizeQuality(int quality) {
        return Math.max(0, Math.min(quality, 100));
    }
}
