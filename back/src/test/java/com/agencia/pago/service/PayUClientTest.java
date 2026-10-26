package com.agencia.service;

import com.agencia.config.PaymentProperties;
import com.agencia.domain.PagoProveedor;
import com.agencia.domain.dto.PasarelaCobroRequestDTO;
import com.agencia.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayUClientStubTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void crearCobro_debeReemplazarPlaceholderIdEnResponseUrl() {
        PaymentProperties properties = new PaymentProperties(
                true,
                PagoProveedor.PAYU,
                null,
                "https://agencia.williamwadde.work/mis-reservas/resultado-pago",
                null,
                "COP",
                new PaymentProperties.PayU(
                        true,
                        "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/",
                        "login",
                        "api-key",
                        "merchant",
                        "account",
                        "public",
                        "https://agencia.williamwadde.work/mis-reservas/{id}/resultado-pago",
                        "https://agencia.williamwadde.work/api/pagos/webhook/payu",
                        new BigDecimal("1.00"),
                        Map.of("COP", new BigDecimal("4000.00"))
                )
        );

        PayUClientStub client = new PayUClientStub(properties, objectMapper);

        var response = client.crearCobro(new PasarelaCobroRequestDTO(
                9L,
                new BigDecimal("10000.00"),
                "COP",
                "Reserva 9",
                "idem-9"
        ));

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.readValue(response.payloadCrudo(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, String> formFields = (Map<String, String>) payload.get("formFields");
        String responseUrl = formFields.get("responseUrl");

        assertTrue(responseUrl.startsWith("https://agencia.williamwadde.work/mis-reservas/9/resultado-pago"));
        assertTrue(responseUrl.contains("reservaId=9"));
        assertTrue(responseUrl.contains("referenceCode=" + response.referenciaExterna()));
    }

    @Test
    void crearCobro_conPlaceholderIdSinReservaId_debeFallar() {
        PaymentProperties properties = new PaymentProperties(
                true,
                PagoProveedor.PAYU,
                null,
                null,
                null,
                "COP",
                new PaymentProperties.PayU(
                        true,
                        "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/",
                        "login",
                        "api-key",
                        "merchant",
                        "account",
                        "public",
                        "https://agencia.williamwadde.work/mis-reservas/{id}/resultado-pago",
                        "https://agencia.williamwadde.work/api/pagos/webhook/payu",
                        new BigDecimal("1.00"),
                        Map.of("COP", new BigDecimal("4000.00"))
                )
        );

        PayUClientStub client = new PayUClientStub(properties, objectMapper);

        PasarelaCobroRequestDTO request = new PasarelaCobroRequestDTO(
                null,
                new BigDecimal("10000.00"),
                "COP",
                "Reserva sin id",
                "idem-null"
        );

        ReglaNegocioException exception = assertThrows(ReglaNegocioException.class,
                () -> client.crearCobro(request));

        assertEquals("PayU response-url contiene {id}, pero no se recibió reservaId", exception.getMessage());
    }
}


