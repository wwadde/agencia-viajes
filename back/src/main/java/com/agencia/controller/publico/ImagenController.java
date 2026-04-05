package com.agencia.controller.publico;

import com.agencia.domain.dto.ImagenBibliotecaItemDTO;
import com.agencia.domain.dto.ImagenSubidaResponse;
import com.agencia.service.StorageService;
import com.agencia.service.StorageFileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la gestión de imágenes del panel de administración.
 *
 * <p>Las imágenes se almacenan en un volumen Docker compartido con Nginx,
 * quien las sirve directamente como contenido estático en producción.
 *
 * <p>Todos los endpoints requieren rol {@code ADMIN}.
 */
@RestController
@RequestMapping("/api/admin/imagenes")
@RequiredArgsConstructor
@Tag(name = "Imágenes", description = "Gestión de imágenes para el contenido del sitio")
@SecurityRequirement(name = "cookieAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class ImagenController {

    private final StorageService storageService;

    /**
     * URL base pública desde la que Nginx sirve las imágenes.
     * Ejemplo: {@code https://midominio.com/images}
     */
    @Value("${storage.public-base-url}")
    private String publicBaseUrl;

    @Value("${storage.thumbnail-suffix:_thumb}")
    private String thumbnailSuffix;


    /**
     * Sube una imagen al servidor.
     *
     * <p>Acepta únicamente imágenes JPEG, PNG y WebP. El tamaño máximo se configura
     * mediante {@code storage.max-file-size-bytes}.
     *
     * @param file archivo de imagen enviado como {@code multipart/form-data}
     * @return {@code 201 Created} con la URL pública de la imagen
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir imagen",
            description = "Sube una imagen JPEG, PNG o WebP al servidor. Requiere rol ADMIN."
    )
    @ApiResponse(responseCode = "201", description = "Imagen subida exitosamente")
    @ApiResponse(responseCode = "400", description = "Archivo inválido (tipo, tamaño o firma incorrecta)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "500", description = "Error interno al guardar el archivo", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ImagenSubidaResponse> subir(
            @Parameter(description = "Carpeta destino permitida (paquetes, iconos, landing)", required = true, example = "paquetes")
            @RequestParam("carpeta") String carpeta,
            @Parameter(description = "Imagen a subir (JPEG, PNG o WebP)", required = true)
            @RequestParam("file") MultipartFile file) {

        String referencia = storageService.guardar(file, carpeta);
        String url = buildPublicUrl(referencia);
        String thumbnailReferencia = toThumbnailReference(referencia);
        String thumbnailUrl = buildPublicUrl(thumbnailReferencia);
        return ResponseEntity.status(201)
                .body(new ImagenSubidaResponse(referencia, url, thumbnailReferencia, thumbnailUrl));
    }

    @GetMapping
    @Operation(
            summary = "Listar imágenes",
            description = "Lista imágenes almacenadas para la biblioteca admin. Permite filtrar por carpeta y nombre."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ImagenBibliotecaItemDTO>> listar(
            @Parameter(description = "Carpeta opcional (paquetes, iconos, landing)", example = "paquetes")
            @RequestParam(value = "carpeta", required = false) String carpeta,
            @Parameter(description = "Búsqueda opcional por nombre de archivo", example = "banner")
            @RequestParam(value = "q", required = false) String busqueda) {

        List<ImagenBibliotecaItemDTO> respuesta = storageService.listar(carpeta, busqueda)
                .stream()
                .map(this::toBibliotecaItem)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Elimina una imagen del servidor por su referencia relativa.
     *
     * @param referencia referencia tal como fue devuelta al subir la imagen
     * @return {@code 204 No Content}
     */
    @DeleteMapping
    @Operation(
            summary = "Eliminar imagen",
            description = "Elimina una imagen del almacenamiento. Requiere rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagen eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Referencia relativa a eliminar", example = "paquetes/abc123.jpg")
            @RequestParam("referencia") String referencia) {

        storageService.eliminar(referencia);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si una imagen existe en el servidor.
     *
     * @param referencia referencia relativa a consultar
     * @return {@code 200 OK} con {@code true} o {@code false}
     */
    @GetMapping("/existe")
    @Operation(
            summary = "Verificar existencia de imagen",
            description = "Indica si el archivo especificado está disponible en el servidor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Boolean> existe(
            @Parameter(description = "Referencia relativa a verificar", example = "paquetes/abc123.jpg")
            @RequestParam("referencia") String referencia) {

        return ResponseEntity.ok(storageService.existe(referencia));
    }

    private String buildPublicUrl(String referencia) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.isBlank() ? referencia : base + "/" + referencia;
    }

    private ImagenBibliotecaItemDTO toBibliotecaItem(StorageFileInfo info) {
        String thumbnailReferencia = toThumbnailReference(info.referencia());
        return new ImagenBibliotecaItemDTO(
                info.referencia(),
                buildPublicUrl(info.referencia()),
                thumbnailReferencia,
                buildPublicUrl(thumbnailReferencia),
                info.carpeta(),
                info.nombre(),
                info.tamanoBytes(),
                info.fechaModificacion()
        );
    }

    private String toThumbnailReference(String referencia) {
        String safeSuffix = (thumbnailSuffix == null || thumbnailSuffix.isBlank()) ? "_thumb" : thumbnailSuffix;
        int slashIndex = referencia.lastIndexOf('/');
        int dotIndex = referencia.lastIndexOf('.');
        if (slashIndex < 0 || dotIndex <= slashIndex) {
            return referencia;
        }
        String prefix = referencia.substring(0, dotIndex);
        String extension = referencia.substring(dotIndex);
        return prefix + safeSuffix + extension;
    }
}
