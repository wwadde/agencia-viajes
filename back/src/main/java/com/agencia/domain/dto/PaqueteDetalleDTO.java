package com.agencia.domain.dto;

import java.util.List;

public record PaqueteDetalleDTO(
        Long id,
        String nombre,
        String slug,
        String descripcion,
        String detalle,
        Integer duracionDias,
        boolean disponible,
        boolean destacado,
        DestinoDTO destino,
        List<ImagenPaqueteDTO> imagenes,
        List<SalidaDTO> salidas
) {
}

