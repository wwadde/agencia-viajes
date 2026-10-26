package com.agencia.service;

import com.agencia.domain.dto.HomeDataDTO;
import com.agencia.domain.dto.HomeLandingSlideDTO;
import com.agencia.domain.dto.ImagenBibliotecaItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeDataService {

    private static final String LANDING_DIRECTORY = "landing";

    private final PaqueteService paqueteService;
    private final ImagenAdminService imagenAdminService;

    public HomeDataDTO obtenerHomeData() {
        return new HomeDataDTO(
                paqueteService.listarPublicos(),
                listarSlidesLanding()
        );
    }

    private List<HomeLandingSlideDTO> listarSlidesLanding() {
        return imagenAdminService.listar(LANDING_DIRECTORY, "").stream()
                .map(this::toSlide)
                .toList();
    }

    private HomeLandingSlideDTO toSlide(ImagenBibliotecaItemDTO item) {
        return new HomeLandingSlideDTO(
                item.referencia(),
                item.url(),
                item.thumbnailSmallUrl(),
                item.thumbnailMediumUrl(),
                item.nombre()
        );
    }
}
