package com.agencia.pago.dto;

import com.agencia.pago.entity.PagoEstado;
import com.agencia.pago.entity.PagoProveedor;

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

