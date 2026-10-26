package com.agencia.pago.service;

import com.agencia.pago.config.PaymentProperties;
import com.agencia.pago.dto.PagoCrearIntentoDTO;
import com.agencia.pago.dto.PasarelaCobroResponseDTO;
import com.agencia.pago.dto.PayUWebhookRequestDTO;
import com.agencia.pago.entity.Pago;
import com.agencia.pago.entity.PagoEstado;
import com.agencia.pago.entity.PagoProveedor;
import com.agencia.pago.repository.PagoRepository;
import com.agencia.reserva.entity.Reserva;
import com.agencia.reserva.entity.ReservaEstado;
import com.agencia.reserva.repository.ReservaRepository;
import com.agencia.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private PayUClient payUClient;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void crearIntento_debePersistirIntentoDePago() {
        Reserva reserva = Reserva.builder()
                .id(100L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("2500.00"))
                .build();

        when(reservaRepository.findByIdAndUsuarioId(100L, 7L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findTopByReservaIdOrderByCreadoEnDesc(100L)).thenReturn(Optional.empty());
        when(paymentProperties.currencyDefault()).thenReturn("COP");
        when(paymentProperties.payu()).thenReturn(payuSinSandbox());
        when(payUClient.crearCobro(any())).thenReturn(new PasarelaCobroResponseDTO(
                "ref-123",
                "https://checkout.local/ref-123",
                "PENDING",
                LocalDateTime.now().plusMinutes(20),
                "{}"
        ));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago pago = inv.getArgument(0);
            pago.setId(1L);
            return pago;
        });

        var result = pagoService.crearIntento(100L, 7L, new PagoCrearIntentoDTO(null, "idem-1"));

        assertEquals(1L, result.pagoId());
        assertEquals(PagoEstado.PENDIENTE, result.estado());
        assertEquals("ref-123", result.referenciaExterna());

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        assertEquals(new BigDecimal("2500.00"), captor.getValue().getMonto());
        assertEquals("COP", captor.getValue().getMoneda());
        assertEquals(PagoProveedor.PAYU, captor.getValue().getProveedor());
    }

    @Test
    void crearIntento_debeReutilizarIntentoSiHayActivoVigente() {
        Reserva reserva = Reserva.builder()
                .id(200L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("300.00"))
                .build();
        Pago pagoActivo = Pago.builder()
                .id(2L)
                .estado(PagoEstado.PENDIENTE)
                .proveedor(PagoProveedor.PAYU)
                .referenciaExterna("ref-activo")
                .urlCheckout("https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/")
                .reserva(reserva)
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();

        when(reservaRepository.findByIdAndUsuarioId(200L, 8L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findTopByReservaIdOrderByCreadoEnDesc(200L)).thenReturn(Optional.of(pagoActivo));
        when(pagoRepository.findByIdempotencyKeyAndReservaId(any(), any())).thenReturn(Optional.empty());

        var result = pagoService.crearIntento(200L, 8L, new PagoCrearIntentoDTO("COP", null));

        assertEquals(2L, result.pagoId());
        assertEquals("ref-activo", result.referenciaExterna());
        assertEquals(null, result.urlCheckout());
        assertEquals(null, result.checkoutPayload());
        verify(payUClient, never()).crearCobro(any());
    }

    @Test
    void crearIntento_debePermitirReintentoSiElActivoYaExpiro() {
        Reserva reserva = Reserva.builder()
                .id(201L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("300.00"))
                .build();
        Pago pagoActivoVencido = Pago.builder()
                .id(20L)
                .estado(PagoEstado.PENDIENTE)
                .expiraEn(LocalDateTime.now().minusMinutes(1))
                .build();

        when(reservaRepository.findByIdAndUsuarioId(201L, 8L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findTopByReservaIdOrderByCreadoEnDesc(201L)).thenReturn(Optional.of(pagoActivoVencido));
        when(paymentProperties.payu()).thenReturn(payuSinSandbox());
        when(payUClient.crearCobro(any())).thenReturn(new PasarelaCobroResponseDTO(
                "ref-201",
                "https://checkout.local/ref-201",
                "PENDING",
                LocalDateTime.now().plusMinutes(20),
                "{}"
        ));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago pago = inv.getArgument(0);
            if (pago.getId() == null) {
                pago.setId(21L);
            }
            return pago;
        });

        var result = pagoService.crearIntento(201L, 8L, new PagoCrearIntentoDTO("COP", "idem-201"));

        assertEquals(21L, result.pagoId());
        assertEquals(PagoEstado.EXPIRADO, pagoActivoVencido.getEstado());
        verify(pagoRepository).save(pagoActivoVencido);
    }

    @Test
    void crearIntento_debeSerIdempotenteCuandoYaExisteMismaIdempotencyKey() {
        Reserva reserva = Reserva.builder()
                .id(202L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("500.00"))
                .build();
        Pago pagoExistente = Pago.builder()
                .id(22L)
                .estado(PagoEstado.PENDIENTE)
                .proveedor(PagoProveedor.PAYU)
                .referenciaExterna("ref-existente")
                .urlCheckout("https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/")
                .idempotencyKey("idem-202")
                .reserva(reserva)
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();

        when(reservaRepository.findByIdAndUsuarioId(202L, 9L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findByIdempotencyKeyAndReservaId("idem-202", 202L)).thenReturn(Optional.of(pagoExistente));

        var result = pagoService.crearIntento(202L, 9L, new PagoCrearIntentoDTO("COP", "idem-202"));

        assertEquals(22L, result.pagoId());
        assertEquals("ref-existente", result.referenciaExterna());
        assertEquals(null, result.urlCheckout());
        assertEquals(null, result.checkoutPayload());
        verify(payUClient, never()).crearCobro(any());
    }

    @Test
    void crearIntento_payuSandbox_debeFallarCuandoMontoEsMenorAlMinimo() {
        Reserva reserva = Reserva.builder()
                .id(410L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("3000.00"))
                .build();

        when(reservaRepository.findByIdAndUsuarioId(410L, 22L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.findTopByReservaIdOrderByCreadoEnDesc(410L)).thenReturn(Optional.empty());
        when(paymentProperties.payu()).thenReturn(new PaymentProperties.PayU(
                true,
                "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/",
                "login",
                "key",
                "merchant",
                "account",
                "public",
                "http://localhost/ok",
                "http://localhost/webhook",
                new BigDecimal("1.00"),
                Map.of("USD", new BigDecimal("1.00"), "COP", new BigDecimal("4000.00"))
        ));

        assertThrows(ReglaNegocioException.class,
                () -> pagoService.crearIntento(410L, 22L, new PagoCrearIntentoDTO("COP", "idem-payu")));
    }

    @Test
    void procesarWebhookPayu_debeConfirmarReservaCuandoFirmaEsValidaYAprobado() {
        Reserva reserva = Reserva.builder()
                .id(510L)
                .estado(ReservaEstado.PENDIENTE)
                .precioTotal(new BigDecimal("9999.00"))
                .build();
        Pago pago = Pago.builder()
                .id(5L)
                .reserva(reserva)
                .estado(PagoEstado.PENDIENTE)
                .monto(new BigDecimal("9999.00"))
                .moneda("COP")
                .referenciaExterna("r510-abc123")
                .build();

        when(paymentProperties.payu()).thenReturn(new PaymentProperties.PayU(
                true,
                "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/",
                "login",
                "4Vj8eK4rloUd272L48hsrarnUA",
                "508029",
                "512321",
                "public",
                "http://localhost/ok",
                "http://localhost/webhook",
                new BigDecimal("1.00"),
                Map.of("USD", new BigDecimal("1.00"), "COP", new BigDecimal("4000.00"))
        ));
        when(pagoRepository.findByReferenciaExterna("r510-abc123")).thenReturn(Optional.of(pago));

        pagoService.procesarWebhookPayU(new PayUWebhookRequestDTO(
                "r510-abc123",
                "4",
                "5a8508ec295b6ecba70412516639cef1",
                "9999.00",
                "COP",
                "4",
                "trx-1",
                "APPROVED",
                "{}"
        ));

        assertEquals(PagoEstado.APROBADO, pago.getEstado());
        assertEquals(ReservaEstado.CONFIRMADA, reserva.getEstado());
        verify(pagoRepository).save(pago);
        verify(reservaRepository).save(reserva);
    }

    private PaymentProperties.PayU payuSinSandbox() {
        return new PaymentProperties.PayU(
                false,
                "https://checkout.payulatam.com/ppp-web-gateway-payu/",
                "login",
                "key",
                "merchant",
                "account",
                "public",
                "http://localhost/ok",
                "http://localhost/webhook",
                new BigDecimal("1.00"),
                Map.of()
        );
    }
}
