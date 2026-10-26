package com.agencia.paquete.dto;

public record ImagenPaqueteDTO(
        Long id,
        String url,
        Integer orden,
        boolean principal
) {
}

