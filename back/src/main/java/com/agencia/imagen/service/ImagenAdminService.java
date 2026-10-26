package com.agencia.service;

import com.agencia.domain.dto.ImagenBibliotecaItemDTO;
import com.agencia.domain.dto.ImagenSubidaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImagenAdminService {

    private static final String MINIATURES_DIRECTORY = "miniaturas";

    private final StorageService storageService;

    @Value("${storage.public-base-url}")
    private String publicBaseUrl;

    public ImagenSubidaResponse subir(String carpeta, MultipartFile file) {
        String referencia = storageService.guardar(file, carpeta);
        String smallThumbnailReferencia = toThumbnailReference(referencia, StorageImageProcessor.SMALL_THUMBNAIL_WIDTH);
        String mediumThumbnailReferencia = toThumbnailReference(referencia, StorageImageProcessor.MEDIUM_THUMBNAIL_WIDTH);
        String smallThumbnailUrl = buildPublicUrl(smallThumbnailReferencia);
        String mediumThumbnailUrl = buildPublicUrl(mediumThumbnailReferencia);

        return new ImagenSubidaResponse(
                referencia,
                buildPublicUrl(referencia),
                smallThumbnailReferencia,
                smallThumbnailUrl,
                smallThumbnailReferencia,
                smallThumbnailUrl,
                mediumThumbnailReferencia,
                mediumThumbnailUrl
        );
    }

    public List<ImagenBibliotecaItemDTO> listar(String carpeta, String busqueda) {
        return storageService.listar(carpeta, busqueda)
                .stream()
                .map(this::toBibliotecaItem)
                .toList();
    }

    public void eliminar(String referencia) {
        storageService.eliminar(referencia);
    }

    public boolean existe(String referencia) {
        return storageService.existe(referencia);
    }

    private String buildPublicUrl(String referencia) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.isBlank()) {
            return referencia;
        }

        String[] segments = referencia.split("/");
        return UriComponentsBuilder.fromUriString(base)
                .pathSegment(segments)
                .build()
                .toUriString();
    }

    private ImagenBibliotecaItemDTO toBibliotecaItem(StorageFileInfo info) {
        String smallThumbnailReferencia = toThumbnailReference(info.referencia(), StorageImageProcessor.SMALL_THUMBNAIL_WIDTH);
        String mediumThumbnailReferencia = toThumbnailReference(info.referencia(), StorageImageProcessor.MEDIUM_THUMBNAIL_WIDTH);
        return new ImagenBibliotecaItemDTO(
                info.referencia(),
                buildPublicUrl(info.referencia()),
                smallThumbnailReferencia,
                buildPublicUrl(smallThumbnailReferencia),
                smallThumbnailReferencia,
                buildPublicUrl(smallThumbnailReferencia),
                mediumThumbnailReferencia,
                buildPublicUrl(mediumThumbnailReferencia),
                info.carpeta(),
                info.nombre(),
                info.tamanoBytes(),
                info.fechaModificacion()
        );
    }

    private String toThumbnailReference(String referencia, int width) {
        int slashIndex = referencia.lastIndexOf('/');
        int dotIndex = referencia.lastIndexOf('.');
        if (slashIndex < 0 || dotIndex <= slashIndex) {
            return referencia;
        }

        String fileName = referencia.substring(slashIndex + 1, dotIndex);
        String extension = referencia.substring(dotIndex);
        return MINIATURES_DIRECTORY + "/" + fileName + "_" + width + "w" + extension;
    }
}

