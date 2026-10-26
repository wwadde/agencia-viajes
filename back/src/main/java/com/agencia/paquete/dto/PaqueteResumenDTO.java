package com.agencia.paquete.dto;

public record PaqueteResumenDTO(
        Long id,
        String nombre,
        String slug,
        String descripcion,
        Integer duracionDias,
        boolean destacado,
        String destino,
        String ciudad,
        String pais,
        String imagenPrincipal
) {
}

