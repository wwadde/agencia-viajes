package com.agencia.domain.dto;

import com.agencia.domain.PagoEstado;
import com.agencia.domain.PagoProveedor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResumenDTO(
        Long id,
        Long reservaId,
        PagoEstado estado,
        PagoProveedor proveedor,
        BigDecimal monto,
        String moneda,
        String referenciaExterna,
        String urlCheckout,
        LocalDateTime creadoEn,
        LocalDateTime confirmadoEn,
        LocalDateTime expiraEn
) {
}

