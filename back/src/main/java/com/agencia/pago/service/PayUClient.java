package com.agencia.pago.service;

import com.agencia.pago.config.PaymentProperties;
import com.agencia.pago.dto.PasarelaCobroRequestDTO;
import com.agencia.pago.dto.PasarelaCobroResponseDTO;
import com.agencia.pago.dto.PayUCheckoutRequestDTO;
import com.agencia.pago.dto.PayUCheckoutResponseDTO;
import com.agencia.shared.exception.ReglaNegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PayUClient {

    private static final String RESPONSE_URL_ID_PLACEHOLDER = "{id}";

    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public PasarelaCobroResponseDTO crearCobro(PasarelaCobroRequestDTO request) {
        PaymentProperties.PayU payu = paymentProperties.payu();
        validarConfiguracion(payu);

        String referencia = construirReferencia(request);
        String moneda = request.moneda().trim().toUpperCase(Locale.ROOT);
        BigDecimal montoNormalizado = request.monto().setScale(2, RoundingMode.HALF_UP);

        String firma = firmaCheckout(
                payu.apiKey(),
                payu.merchantId(),
                referencia,
                montoNormalizado,
                moneda
        );

        PayUCheckoutRequestDTO checkoutRequest = new PayUCheckoutRequestDTO(
                payu.merchantId(),
                payu.accountId(),
                referencia,
                request.descripcion(),
                montoNormalizado,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                moneda,
                firma,
                payu.sandboxEnabled(),
                null,
                resolveResponseUrl(payu, request.reservaId(), referencia),
                resolveConfirmationUrl(payu)
        );

        Map<String, String> formFields = construirCamposFormulario(checkoutRequest);
        String checkoutUrl = payu.checkoutBaseUrl();
        log.info("[PAYU] Checkout generado reserva={} referencia={} monto={} {}",
                request.reservaId(), referencia, montoNormalizado, moneda);

        PayUCheckoutResponseDTO checkoutResponse = new PayUCheckoutResponseDTO(
                referencia,
                checkoutUrl,
                "POST",
                formFields,
                firma,
                montoNormalizado,
                moneda,
                payu.sandboxEnabled()
        );

        return new PasarelaCobroResponseDTO(
                referencia,
                checkoutUrl,
                "PENDING",
                LocalDateTime.now().plusMinutes(30),
                payloadCrudo(checkoutResponse)
        );
    }

    private void validarConfiguracion(PaymentProperties.PayU payu) {
        if (isBlank(payu.apiKey()) || isBlank(payu.merchantId()) || isBlank(payu.accountId())) {
            throw new ReglaNegocioException("PayU no está configurado. Defina api-key, merchant-id y account-id en payments.payu.*");
        }
    }

    private String construirReferencia(PasarelaCobroRequestDTO request) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            String limpio = request.idempotencyKey().replaceAll("[^a-zA-Z0-9]", "");
            if (!limpio.isBlank()) {
                return ("R" + request.reservaId() + limpio).substring(0, Math.min(60, ("R" + request.reservaId() + limpio).length()));
            }
        }
        return "R" + request.reservaId() + suffix;
    }

    private String resolveResponseUrl(PaymentProperties.PayU payu, Long reservaId, String referenceCode) {
        String baseUrl;
        if (!isBlank(payu.responseUrl())) {
            baseUrl = payu.responseUrl().trim();
        } else if (!isBlank(paymentProperties.callbackSuccessUrl())) {
            baseUrl = paymentProperties.callbackSuccessUrl().trim();
        } else {
            return null;
        }

        if (baseUrl.contains(RESPONSE_URL_ID_PLACEHOLDER)) {
            if (reservaId == null) {
                throw new ReglaNegocioException("PayU response-url contiene {id}, pero no se recibió reservaId");
            }
            baseUrl = baseUrl.replace(RESPONSE_URL_ID_PLACEHOLDER, String.valueOf(reservaId));
        }

        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("reservaId", reservaId)
                .queryParam("referenceCode", referenceCode)
                .build(true)
                .toUriString();
    }

    private String resolveConfirmationUrl(PaymentProperties.PayU payu) {
        if (!isBlank(payu.confirmationUrl())) {
            return payu.confirmationUrl().trim();
        }
        throw new ReglaNegocioException("PayU requiere payments.payu.confirmation-url configurada y pública para confirmar pagos");
    }

    private Map<String, String> construirCamposFormulario(PayUCheckoutRequestDTO dto) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("merchantId", dto.merchantId());
        fields.put("accountId", dto.accountId());
        fields.put("description", dto.description());
        fields.put("referenceCode", dto.referenceCode());
        fields.put("amount", dto.amount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("tax", dto.tax().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("taxReturnBase", dto.taxReturnBase().setScale(2, RoundingMode.HALF_UP).toPlainString());
        fields.put("currency", dto.currency());
        fields.put("signature", dto.signature());
        fields.put("test", dto.test() ? "1" : "0");
        fields.put("lng", "es");

        if (!isBlank(dto.buyerEmail())) {
            fields.put("buyerEmail", dto.buyerEmail());
        }
        if (!isBlank(dto.responseUrl())) {
            fields.put("responseUrl", dto.responseUrl());
        }
        if (!isBlank(dto.confirmationUrl())) {
            fields.put("confirmationUrl", dto.confirmationUrl());
        }

        return fields;
    }

    private String firmaCheckout(String apiKey, String merchantId, String referenceCode, BigDecimal amount, String currency) {
        String raw = apiKey + "~" + merchantId + "~" + referenceCode + "~"
                + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "~" + currency;
        return md5(raw);
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No fue posible calcular firma MD5 para PayU", ex);
        }
    }

    private String payloadCrudo(PayUCheckoutResponseDTO response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException ignored) {
            return "{\"provider\":\"payu\",\"reference\":\"" + response.referenceCode() + "\"}";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

