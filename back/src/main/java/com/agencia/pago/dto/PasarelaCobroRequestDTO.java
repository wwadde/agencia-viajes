package com.agencia.pago.dto;

import java.math.BigDecimal;

public record PasarelaCobroRequestDTO(
        Long reservaId,
        BigDecimal monto,
        String moneda,
        String descripcion,
        String idempotencyKey
) {
}

