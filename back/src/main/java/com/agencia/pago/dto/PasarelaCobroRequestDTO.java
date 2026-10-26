package com.agencia.domain.dto;

import java.math.BigDecimal;

public record PasarelaCobroRequestDTO(
        Long reservaId,
        BigDecimal monto,
        String moneda,
        String descripcion,
        String idempotencyKey
) {
}

