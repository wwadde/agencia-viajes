package com.agencia.service;

import com.agencia.config.PaymentProperties;
import com.agencia.domain.Pago;
import com.agencia.domain.PagoEstado;
import com.agencia.domain.PagoProveedor;
import com.agencia.domain.Reserva;
import com.agencia.domain.ReservaEstado;
import com.agencia.domain.dto.PagoCrearIntentoDTO;
import com.agencia.domain.dto.PagoIntentoDTO;
import com.agencia.domain.dto.PagoResumenDTO;
import com.agencia.domain.dto.PagoWebhookDTO;
import com.agencia.domain.dto.PayUWebhookRequestDTO;
import com.agencia.domain.dto.PasarelaCobroRequestDTO;
import com.agencia.domain.dto.PasarelaCobroResponseDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.PagoRepository;
import com.agencia.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoService {

    private static final Set<PagoEstado> ESTADOS_ACTIVOS = Set.of(PagoEstado.PENDIENTE, PagoEstado.REQUIERE_ACCION);

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final PasarelaPagoClientFactory pasarelaPagoClientFactory;
    private final PaymentProperties paymentProperties;

    @Transactional
    public PagoIntentoDTO crearIntento(Long reservaId, Long usuarioId, PagoProveedor proveedor, PagoCrearIntentoDTO dto) {
        Reserva reserva = getReservaMiaOrThrow(reservaId, usuarioId);

        if (reserva.getEstado() == ReservaEstado.CANCELADA) {
            throw new ReglaNegocioException("No puede iniciar pagos sobre una reserva cancelada");
        }

        String idempotencyKey = resolveIdempotencyKey(dto);
        var existentePorIdempotencia = pagoRepository.findByIdempotencyKeyAndReservaId(idempotencyKey, reservaId);
        if (existentePorIdempotencia.isPresent()) {
            return toIntentoDTO(existentePorIdempotencia.get(), true);
        }

        var intentoActivoReutilizable = resolverIntentoActivoSiEstaVencido(reservaId);
        if (intentoActivoReutilizable != null) {
            return toIntentoDTO(intentoActivoReutilizable, true);
        }

        PagoProveedor proveedorSeleccionado = resolveProveedor(proveedor);
        PasarelaPagoClient pasarelaClient = pasarelaPagoClientFactory.getClient(proveedorSeleccionado);

        String moneda = resolveMoneda(dto);
        validarMontoSandboxPayU(proveedorSeleccionado, reserva.getPrecioTotal(), moneda);

        PasarelaCobroResponseDTO response = pasarelaClient.crearCobro(
                new PasarelaCobroRequestDTO(
                        reserva.getId(),
                        reserva.getPrecioTotal(),
                        moneda,
                        "Reserva " + reserva.getId() + " - Agencia",
                        idempotencyKey
                )
        );

        Pago pago = Pago.builder()
                .reserva(reserva)
                .estado(mapEstadoPasarela(response.estadoInicial()))
                .proveedor(proveedorSeleccionado)
                .monto(reserva.getPrecioTotal())
                .moneda(moneda)
                .referenciaExterna(response.referenciaExterna())
                .urlCheckout(response.urlCheckout())
                .idempotencyKey(idempotencyKey)
                .payloadPasarela(response.payloadCrudo())
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .expiraEn(response.expiraEn())
                .build();

        try {
            Pago saved = pagoRepository.save(pago);
            return toIntentoDTO(saved, false);
        } catch (DataIntegrityViolationException ex) {
            return pagoRepository.findByIdempotencyKeyAndReservaId(idempotencyKey, reservaId)
                    .map(existing -> toIntentoDTO(existing, true))
                    .orElseThrow(() -> ex);
        }
    }

    private Pago resolverIntentoActivoSiEstaVencido(Long reservaId) {
        var existingOpt = pagoRepository.findTopByReservaIdOrderByCreadoEnDesc(reservaId)
                .filter(existing -> ESTADOS_ACTIVOS.contains(existing.getEstado()));

        if (existingOpt.isEmpty()) {
            return null;
        }

        Pago existing = existingOpt.get();
        if (existing.getExpiraEn() != null && !existing.getExpiraEn().isAfter(LocalDateTime.now())) {
            existing.setEstado(PagoEstado.EXPIRADO);
            existing.setActualizadoEn(LocalDateTime.now());
            pagoRepository.save(existing);
            return null;
        }

        return existing;
    }

    public List<PagoResumenDTO> listarMios(Long reservaId, Long usuarioId) {
        Reserva reserva = getReservaMiaOrThrow(reservaId, usuarioId);
        return pagoRepository.findByReservaIdOrderByCreadoEnDesc(reserva.getId()).stream()
                .map(this::toResumenDTO)
                .toList();
    }

    public List<PagoResumenDTO> listarAdminPorReserva(Long reservaId) {
        getReservaOrThrow(reservaId);
        return pagoRepository.findByReservaIdOrderByCreadoEnDesc(reservaId).stream()
                .map(this::toResumenDTO)
                .toList();
    }

    @Transactional
    public void procesarWebhook(PagoWebhookDTO dto) {
        // TODO Validar firma HMAC/JWS del proveedor con payments.webhook-secret antes de aceptar el webhook.
        // TODO Validar replay attacks (timestamp + nonce) y usar idempotencia por evento del proveedor.

        Pago pago = pagoRepository.findByReferenciaExterna(dto.referenciaExterna())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para referencia: " + dto.referenciaExterna()));

        PagoEstado nuevoEstado = mapEstadoPasarela(dto.estadoPasarela());

        pago.setEstado(nuevoEstado);
        pago.setPayloadPasarela(dto.payload());
        pago.setActualizadoEn(LocalDateTime.now());

        if (nuevoEstado == PagoEstado.APROBADO) {
            pago.setConfirmadoEn(LocalDateTime.now());
            confirmarReservaSiAplica(pago.getReserva());
        }

        pagoRepository.save(pago);
    }

    @Transactional
    public void procesarWebhookPayU(PayUWebhookRequestDTO dto) {
        if (dto.referenceSale() == null || dto.referenceSale().isBlank()) {
            throw new ReglaNegocioException("La referencia de PayU es obligatoria");
        }
        validarFirmaPayU(dto);

        Pago pago = pagoRepository.findByReferenciaExterna(dto.referenceSale())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para referencia: " + dto.referenceSale()));

        validarMontoYMonedaWebhookPayU(pago, dto);

        PagoEstado nuevoEstado = mapEstadoPayU(dto.statePol(), dto.transactionState());
        pago.setEstado(nuevoEstado);
        pago.setPayloadPasarela(dto.payload());
        pago.setActualizadoEn(LocalDateTime.now());

        if (nuevoEstado == PagoEstado.APROBADO) {
            pago.setConfirmadoEn(LocalDateTime.now());
            confirmarReservaSiAplica(pago.getReserva());
        }

        pagoRepository.save(pago);
    }

    @Transactional
    public void expirarPendientesVencidos() {
        // TODO Programar un job @Scheduled para ejecutar este método cada pocos minutos.
        List<Pago> pendientes = pagoRepository.findByEstadoOrderByCreadoEnDesc(PagoEstado.PENDIENTE);
        LocalDateTime now = LocalDateTime.now();

        for (Pago pago : pendientes) {
            if (pago.getExpiraEn() != null && pago.getExpiraEn().isBefore(now)) {
                pago.setEstado(PagoEstado.EXPIRADO);
                pago.setActualizadoEn(now);
                pagoRepository.save(pago);
            }
        }
    }

    private void confirmarReservaSiAplica(Reserva reserva) {
        if (reserva.getEstado() == ReservaEstado.PENDIENTE) {
            reserva.setEstado(ReservaEstado.CONFIRMADA);
            // TODO Si la pasarela soporta pago parcial, mover esta lógica a una política de consolidación de pagos.
            reservaRepository.save(reserva);
        }
    }

    private Reserva getReservaMiaOrThrow(Long reservaId, Long usuarioId) {
        return reservaRepository.findByIdAndUsuarioId(reservaId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + reservaId));
    }

    private Reserva getReservaOrThrow(Long reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + reservaId));
    }

    private PagoProveedor resolveProveedor(PagoProveedor proveedor) {
        if (proveedor != null) {
            return proveedor;
        }
        if (paymentProperties.provider() != null) {
            return paymentProperties.provider();
        }
        return PagoProveedor.SIN_CONFIGURAR;
    }

    private String resolveMoneda(PagoCrearIntentoDTO dto) {
        String moneda = dto != null ? dto.moneda() : null;
        if (moneda == null || moneda.isBlank()) {
            return paymentProperties.currencyDefault();
        }
        return moneda.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveIdempotencyKey(PagoCrearIntentoDTO dto) {
        if (dto != null && dto.idempotencyKey() != null && !dto.idempotencyKey().isBlank()) {
            return dto.idempotencyKey().trim();
        }
        // TODO Reemplazar por llave determinística (por usuario/reserva) cuando se conecte la pasarela real.
        return UUID.randomUUID().toString();
    }

    private void validarMontoSandboxPayU(PagoProveedor proveedor, BigDecimal monto, String moneda) {
        if (proveedor != PagoProveedor.PAYU || !paymentProperties.payu().sandboxEnabled()) {
            return;
        }

        BigDecimal minimo = paymentProperties.payu().minimumAmountForCurrency(moneda);
        if (minimo == null) {
            throw new ReglaNegocioException("No hay monto mínimo configurado para PayU Sandbox en la moneda " + moneda
                    + ". Configure payments.payu.sandbox-min-amount-by-currency.");
        }

        if (monto.compareTo(minimo) < 0) {
            throw new ReglaNegocioException("En PayU Sandbox el monto mínimo para " + moneda + " es " + minimo
                    + " (equivalente a al menos 1 USD en pruebas).");
        }
    }

    private void validarFirmaPayU(PayUWebhookRequestDTO dto) {
        PaymentProperties.PayU payu = paymentProperties.payu();
        if (payu.apiKey() == null || payu.apiKey().isBlank() || payu.merchantId() == null || payu.merchantId().isBlank()) {
            throw new ReglaNegocioException("PayU no está configurado para validar la firma del webhook");
        }

        if (dto.sign() == null || dto.sign().isBlank()) {
            throw new ReglaNegocioException("Firma PayU inválida");
        }

        String stateForSignature = resolveStateForSignature(dto);
        boolean firmaValida = firmaPayUCoincide(
                payu.apiKey(),
                payu.merchantId(),
                dto.referenceSale(),
                dto.value(),
                dto.currency(),
                stateForSignature,
                dto.sign()
        );
        if (!firmaValida) {
            throw new ReglaNegocioException("Firma PayU inválida");
        }
    }

    private boolean firmaPayUCoincide(String apiKey, String merchantId, String referenceSale, String value,
                                      String currency, String statePol, String firmaRecibida) {
        String[] valueCandidates = normalizarValoresFirmaPayU(value);
        for (String valueCandidate : valueCandidates) {
            String raw = apiKey + "~" + merchantId + "~" + referenceSale + "~" + valueCandidate + "~"
                    + currency.trim().toUpperCase(Locale.ROOT) + "~" + statePol.trim();
            String hashed = md5(raw);
            if (hashed.equalsIgnoreCase(firmaRecibida.trim())) {
                return true;
            }
        }
        return false;
    }

    private String resolveStateForSignature(PayUWebhookRequestDTO dto) {
        if (dto.statePol() != null && !dto.statePol().isBlank()) {
            return dto.statePol().trim();
        }
        if (dto.transactionState() != null && !dto.transactionState().isBlank()) {
            return dto.transactionState().trim();
        }
        throw new ReglaNegocioException("No se recibió state_pol/transactionState en webhook PayU");
    }

    private void validarMontoYMonedaWebhookPayU(Pago pago, PayUWebhookRequestDTO dto) {
        if (dto.currency() == null || dto.currency().isBlank()) {
            throw new ReglaNegocioException("La moneda de PayU es obligatoria");
        }
        if (dto.value() == null || dto.value().isBlank()) {
            throw new ReglaNegocioException("El valor de PayU es obligatorio");
        }

        String monedaWebhook = dto.currency().trim().toUpperCase(Locale.ROOT);
        String monedaPago = pago.getMoneda() == null ? "" : pago.getMoneda().trim().toUpperCase(Locale.ROOT);
        if (!monedaWebhook.equals(monedaPago)) {
            throw new ReglaNegocioException("La moneda del webhook PayU no coincide con el pago registrado");
        }

        BigDecimal valorWebhook = parsearMonto(dto.value());
        BigDecimal valorPago = pago.getMonto().setScale(2, RoundingMode.HALF_UP);
        if (valorWebhook.compareTo(valorPago) != 0) {
            throw new ReglaNegocioException("El monto del webhook PayU no coincide con el pago registrado");
        }
    }

    private BigDecimal parsearMonto(String value) {
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException("El monto del webhook PayU es inválido");
        }
    }

    private String[] normalizarValoresFirmaPayU(String value) {
        try {
            BigDecimal decimal = new BigDecimal(value.trim());
            return new String[]{
                    decimal.toPlainString(),
                    decimal.setScale(1, RoundingMode.HALF_UP).toPlainString(),
                    decimal.setScale(2, RoundingMode.HALF_UP).toPlainString()
            };
        } catch (NumberFormatException ex) {
            return new String[]{value};
        }
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No fue posible calcular firma MD5 de PayU", ex);
        }
    }

    private PagoEstado mapEstadoPayU(String statePol, String transactionState) {
        if (statePol != null && !statePol.isBlank()) {
            return switch (statePol.trim()) {
                case "4" -> PagoEstado.APROBADO;
                case "5" -> PagoEstado.EXPIRADO;
                case "6", "104", "101", "102", "103" -> PagoEstado.RECHAZADO;
                case "7" -> PagoEstado.PENDIENTE;
                default -> mapEstadoPasarela(statePol);
            };
        }
        return mapEstadoPasarela(transactionState);
    }

    private PagoEstado mapEstadoPasarela(String estadoPasarela) {
        if (estadoPasarela == null || estadoPasarela.isBlank()) {
            return PagoEstado.PENDIENTE;
        }

        String normalized = estadoPasarela.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "PENDING", "IN_PROCESS", "CREATED" -> PagoEstado.PENDIENTE;
            case "REQUIRES_ACTION", "ACTION_REQUIRED" -> PagoEstado.REQUIERE_ACCION;
            case "APPROVED", "PAID", "SUCCEEDED" -> PagoEstado.APROBADO;
            case "REJECTED", "FAILED", "DECLINED" -> PagoEstado.RECHAZADO;
            case "CANCELLED", "CANCELED" -> PagoEstado.CANCELADO;
            case "EXPIRED" -> PagoEstado.EXPIRADO;
            default -> PagoEstado.PENDIENTE;
        };
    }

    private PagoIntentoDTO toIntentoDTO(Pago pago, boolean reutilizado) {
        // Cuando el intento se reutiliza no reenviamos el payload de checkout para evitar repost del mismo referenceCode en PayU.
        String urlCheckout = reutilizado ? null : pago.getUrlCheckout();
        String checkoutPayload = reutilizado ? null : pago.getPayloadPasarela();
        return new PagoIntentoDTO(
                pago.getId(),
                pago.getEstado(),
                pago.getProveedor(),
                pago.getReferenciaExterna(),
                urlCheckout,
                pago.getExpiraEn(),
                checkoutPayload
        );
    }

    private PagoResumenDTO toResumenDTO(Pago pago) {
        return new PagoResumenDTO(
                pago.getId(),
                pago.getReserva() != null ? pago.getReserva().getId() : null,
                pago.getEstado(),
                pago.getProveedor(),
                pago.getMonto(),
                pago.getMoneda(),
                pago.getReferenciaExterna(),
                pago.getUrlCheckout(),
                pago.getCreadoEn(),
                pago.getConfirmadoEn(),
                pago.getExpiraEn()
        );
    }
}

