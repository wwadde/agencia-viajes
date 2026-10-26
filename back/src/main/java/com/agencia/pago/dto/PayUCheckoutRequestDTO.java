package com.agencia.pago.dto;

import java.math.BigDecimal;

public record PayUCheckoutRequestDTO(
        String merchantId,
        String accountId,
        String referenceCode,
        String description,
        BigDecimal amount,
        BigDecimal tax,
        BigDecimal taxReturnBase,
        String currency,
        String signature,
        boolean test,
        String buyerEmail,
        String responseUrl,
        String confirmationUrl
) {
}

