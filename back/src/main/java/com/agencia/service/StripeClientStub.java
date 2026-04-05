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
public class StripeClientStub implements PasarelaPagoClient {

    @Override
    public PagoProveedor proveedor() {
        return PagoProveedor.STRIPE;
    }

    @Override
    public PasarelaCobroResponseDTO crearCobro(PasarelaCobroRequestDTO request) {
        // TODO Reemplazar este stub por Checkout Session real de Stripe.
        String referencia = "st-" + UUID.randomUUID();
        log.info("[STUB-STRIPE] Creando cobro reserva={} monto={} {}", request.reservaId(), request.monto(), request.moneda());

        return new PasarelaCobroResponseDTO(
                referencia,
                "https://checkout.stripe.local/session/" + referencia,
                "REQUIRES_ACTION",
                LocalDateTime.now().plusMinutes(30),
                "{\"provider\":\"stripe\",\"status\":\"REQUIRES_ACTION\"}"
        );
    }
}

