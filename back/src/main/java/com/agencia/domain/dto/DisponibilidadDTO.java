package com.agencia.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DisponibilidadDTO(
        Long paqueteId,
        String paquete,
        LocalDate fechaSalida,
        BigDecimal precioPorPersona,
        Integer cuposDisponibles,
        Integer personasSolicitadas,
        boolean disponible
) {
}
