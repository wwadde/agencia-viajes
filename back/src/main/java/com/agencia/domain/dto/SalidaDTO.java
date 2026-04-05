package com.agencia.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalidaDTO(
        Long id,
        LocalDate fechaSalida,
        BigDecimal precio,
        Integer cuposDisponibles
) {
}

