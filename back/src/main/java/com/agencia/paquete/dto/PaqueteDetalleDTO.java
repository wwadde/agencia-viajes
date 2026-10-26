package com.agencia.paquete.dto;

import tools.jackson.databind.JsonNode;

import java.util.List;
import com.agencia.destino.dto.DestinoDTO;

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

