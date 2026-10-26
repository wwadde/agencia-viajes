package com.agencia.agenda.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgendaSalidaDTO(
        Long id,
        LocalDate fechaSalida,
        BigDecimal precio,
        Integer cuposDisponibles,
        Long paqueteId,
        String paqueteNombre,
        String paqueteSlug
) {
}

