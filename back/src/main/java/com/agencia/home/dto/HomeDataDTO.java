package com.agencia.home.dto;

import java.util.List;
import com.agencia.paquete.dto.PaqueteResumenDTO;

public record HomeDataDTO(
        List<PaqueteResumenDTO> paquetes,
        List<HomeLandingSlideDTO> landingSlides
) {
}
