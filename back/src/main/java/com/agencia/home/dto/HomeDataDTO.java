package com.agencia.domain.dto;

import java.util.List;

public record HomeDataDTO(
        List<PaqueteResumenDTO> paquetes,
        List<HomeLandingSlideDTO> landingSlides
) {
}
