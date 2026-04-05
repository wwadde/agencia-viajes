package com.agencia.domain.dto;

import com.agencia.domain.PagoEstado;
import com.agencia.domain.PagoProveedor;

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

