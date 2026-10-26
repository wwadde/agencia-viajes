package com.agencia.config;

import com.agencia.domain.PagoProveedor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ConfigurationProperties(prefix = "payments")
public record PaymentProperties(
        boolean enabled,
        PagoProveedor provider,
        String webhookSecret,
        String callbackSuccessUrl,
        String callbackFailureUrl,
        String currencyDefault,
        PayU payu
) {
    public PaymentProperties {
        if (provider == null) {
            provider = PagoProveedor.SIN_CONFIGURAR;
        }
        if (currencyDefault == null || currencyDefault.isBlank()) {
            currencyDefault = "COP";
        }
        if (payu == null) {
            payu = PayU.defaults();
        }
    }

    public record PayU(
            boolean sandboxEnabled,
            String checkoutBaseUrl,
            String apiLogin,
            String apiKey,
            String merchantId,
            String accountId,
            String publicKey,
            String responseUrl,
            String confirmationUrl,
            BigDecimal sandboxMinUsdAmount,
            Map<String, BigDecimal> sandboxMinAmountByCurrency
    ) {

        public PayU {
            if (checkoutBaseUrl == null || checkoutBaseUrl.isBlank()) {
                checkoutBaseUrl = "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/";
            }
            if (sandboxMinUsdAmount == null || sandboxMinUsdAmount.signum() <= 0) {
                sandboxMinUsdAmount = new BigDecimal("1.00");
            }

            Map<String, BigDecimal> normalized = new HashMap<>();
            if (sandboxMinAmountByCurrency != null) {
                sandboxMinAmountByCurrency.forEach((k, v) -> {
                    if (k != null && !k.isBlank() && v != null && v.signum() > 0) {
                        normalized.put(k.trim().toUpperCase(Locale.ROOT), v);
                    }
                });
            }
            normalized.putIfAbsent("USD", sandboxMinUsdAmount);
            normalized.putIfAbsent("COP", new BigDecimal("4000.00"));
            sandboxMinAmountByCurrency = Map.copyOf(normalized);
        }

        public static PayU defaults() {
            return new PayU(
                    true,
                    "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    new BigDecimal("1.00"),
                    Map.of("USD", new BigDecimal("1.00"), "COP", new BigDecimal("4000.00"))
            );
        }

        public BigDecimal minimumAmountForCurrency(String currency) {
            if (currency == null || currency.isBlank()) {
                return sandboxMinUsdAmount;
            }
            String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
            return sandboxMinAmountByCurrency.get(normalizedCurrency);
        }
    }
}

