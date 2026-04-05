package com.agencia.service;

import com.agencia.domain.PagoProveedor;
import com.agencia.domain.dto.PasarelaCobroRequestDTO;
import com.agencia.domain.dto.PasarelaCobroResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class MercadoPagoClientStub implements PasarelaPagoClient {

    @Override
    public PagoProveedor proveedor() {
        return PagoProveedor.MERCADOPAGO;
    }

    @Override
    public PasarelaCobroResponseDTO crearCobro(PasarelaCobroRequestDTO request) {
        // TODO Reemplazar este stub por la integración real de Mercado Pago (preferencia + checkout).
        String referencia = "mp-" + UUID.randomUUID();
        log.info("[STUB-MP] Creando cobro reserva={} monto={} {}", request.reservaId(), request.monto(), request.moneda());

        return new PasarelaCobroResponseDTO(
                referencia,
                "https://sandbox.mercadopago.local/checkout/" + referencia,
                "PENDING",
                LocalDateTime.now().plusMinutes(30),
                "{\"provider\":\"mercadopago\",\"status\":\"PENDING\"}"
        );
    }
}

