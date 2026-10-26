package com.agencia.domain.dto;

import java.math.BigDecimal;
import java.util.Map;

public record PayUCheckoutResponseDTO(
        String referenceCode,
        String actionUrl,
        String method,
        Map<String, String> formFields,
        String signature,
        BigDecimal amount,
        String currency,
        boolean test
) {
}

