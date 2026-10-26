package com.agencia.domain.dto;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record PaqueteDetalleDTO(
        Long id,
        String nombre,
        String slug,
        String descripcion,
        JsonNode detalle,
        String detalleTextoPlano,
        Integer duracionDias,
        boolean disponible,
        boolean destacado,
        DestinoDTO destino,
        List<DestinoDTO> destinos,
        List<ImagenPaqueteDTO> imagenes,
        List<SalidaDTO> salidas
) {
}

