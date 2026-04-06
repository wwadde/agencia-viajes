package com.agencia.service;

import com.agencia.config.StorageProperties;
import com.agencia.exception.AlmacenamientoException;
import com.agencia.exception.ArchivoInvalidoException;
import com.agencia.exception.ArchivoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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



@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

    private final StoragePathSupport pathSupport;
    private final StorageImageValidator imageValidator;
    private final StorageImageProcessor imageProcessor;

    public FileSystemStorageService(StorageProperties properties) {
        this.pathSupport = new StoragePathSupport(
                properties.location(),
                properties.allowedDirectories(),
                properties.thumbnailSuffix()
        );
        this.imageValidator = new StorageImageValidator(properties.maxFileSizeBytes());
        this.imageProcessor = new StorageImageProcessor(
                properties.webpQuality(),
                properties.thumbnailWidth(),
                properties.thumbnailHeight()
        );
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


        imageValidator.validateSize(file);
        String contentType = imageValidator.validateAndGetContentType(file);
        imageValidator.validateMagicBytes(file, contentType);

        String directory = pathSupport.validateAndNormalizeDirectory(carpeta);
        String filename = pathSupport.buildOptimizedFilename(nombreArchivo);
        String thumbnailFilename = pathSupport.buildThumbnailFilename(nombreArchivo);
        String referencia = directory + "/" + filename;
        String thumbnailReferencia = directory + "/" + thumbnailFilename;
        Path destination = pathSupport.resolveSecureReference(referencia);
        Path thumbnailDestination = pathSupport.resolveSecureReference(thumbnailReferencia);

        imageProcessor.writeOptimizedFiles(
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
        String normalizedReference = pathSupport.normalizeReference(referencia);
        Path target = pathSupport.resolveSecureReference(normalizedReference);
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
        return Files.exists(pathSupport.resolveSecureReference(referencia));
    }

    @Override
    public List<StorageFileInfo> listar(String carpeta, String busqueda) {
        String query = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);

        Set<String> carpetasObjetivo = (carpeta == null || carpeta.isBlank())
                ? pathSupport.allowedDirectories()
                : Set.of(pathSupport.validateAndNormalizeDirectory(carpeta));

        List<StorageFileInfo> items = new ArrayList<>();
        for (String carpetaObjetivo : carpetasObjetivo) {
            Path directoryPath = pathSupport.rootLocation().resolve(carpetaObjetivo).normalize();
            pathSupport.validateRootAccess(directoryPath, carpetaObjetivo);
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

    private void initDirectory() {
        try {
            Files.createDirectories(pathSupport.rootLocation());
            log.info("Directorio de almacenamiento: {}", pathSupport.rootLocation());
        } catch (IOException e) {
            throw new AlmacenamientoException(
                    "No se pudo crear el directorio de almacenamiento: " + pathSupport.rootLocation(), e);
        }
    }

    public String sanitizeFilename(String filename) {
        return pathSupport.sanitizeFilename(filename);
    }

    private void deleteGeneratedPairIfExists(String normalizedReference) throws IOException {
        String thumbnailReference = pathSupport.toThumbnailReference(normalizedReference);
        if (!thumbnailReference.equals(normalizedReference)) {
            Files.deleteIfExists(pathSupport.resolveSecureReference(thumbnailReference));
        }

        String originalReference = pathSupport.toOriginalReferenceFromThumbnail(normalizedReference);
        if (originalReference != null && !originalReference.equals(normalizedReference)) {
            Files.deleteIfExists(pathSupport.resolveSecureReference(originalReference));
        }
    }

    private void appendFileInfo(Path path, String carpeta, String query, List<StorageFileInfo> items) {
        String fileName = path.getFileName().toString();

        // Si hay archivos legacy no válidos en disco, se omiten del listado admin.
        String safeName;
        try {
            safeName = sanitizeFilename(fileName);
        } catch (ArchivoInvalidoException _) {
            log.warn("Archivo omitido de biblioteca por nombre inválido: {}", fileName);
            return;
        }

        if (!safeName.equals(fileName)) {
            return;
        }

        if (pathSupport.isGeneratedThumbnail(fileName)) {
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
}
