package com.agencia.foro.dto;

import com.agencia.foro.entity.ForoBloqueTipo;

public record ForoBloqueDTO(
        Long id,
        ForoBloqueTipo tipo,
        String contenido,
        String pieImagen,
        Integer orden
) {
}
