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
public class PasarelaPagoClientStub implements PasarelaPagoClient {

    @Override
    public PagoProveedor proveedor() {
        return PagoProveedor.SIN_CONFIGURAR;
    }

    @Override
    public PasarelaCobroResponseDTO crearCobro(PasarelaCobroRequestDTO request) {
        // TODO Integrar SDK/API de la pasarela real (Mercado Pago/Stripe/PayU) usando credenciales seguras.
        // TODO Enviar callback URLs (success/failure/pending) según la configuración de payments.*.
        // TODO Propagar idempotencyKey real en header/metadata del proveedor para evitar cobros duplicados.

        String referencia = "stub-" + UUID.randomUUID();
        String checkout = "https://checkout.local/pago/" + referencia;
        log.info("[STUB] Creando cobro simulado reserva={} monto={} {}", request.reservaId(), request.monto(), request.moneda());

        return new PasarelaCobroResponseDTO(
                referencia,
                checkout,
                "PENDING",
                LocalDateTime.now().plusMinutes(30),
                "{\"provider\":\"stub\",\"status\":\"PENDING\"}"
        );
    }
}
