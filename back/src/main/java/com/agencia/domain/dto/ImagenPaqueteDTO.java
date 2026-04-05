package com.agencia.domain.dto;

public record ImagenPaqueteDTO(
        Long id,
        String url,
        Integer orden,
        boolean principal
) {
}

