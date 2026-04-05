package com.agencia.domain.dto;

public record PaqueteResumenDTO(
        Long id,
        String nombre,
        String slug,
        String descripcion,
        Integer duracionDias,
        boolean destacado,
        String destino,
        String imagenPrincipal
) {
}

