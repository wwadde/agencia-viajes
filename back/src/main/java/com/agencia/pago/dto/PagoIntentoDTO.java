package com.agencia.pago.dto;

import com.agencia.pago.entity.PagoEstado;
import com.agencia.pago.entity.PagoProveedor;

import java.time.LocalDateTime;

public record PagoIntentoDTO(
        Long pagoId,
        PagoEstado estado,
        PagoProveedor proveedor,
        String referenciaExterna,
        String urlCheckout,
        LocalDateTime expiraEn,
        String checkoutPayload
) {
}

