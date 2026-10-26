package com.agencia.domain.dto;

import com.agencia.domain.ForoBloqueTipo;

public record ForoBloqueDTO(
        Long id,
        ForoBloqueTipo tipo,
        String contenido,
        String pieImagen,
        Integer orden
) {
}
