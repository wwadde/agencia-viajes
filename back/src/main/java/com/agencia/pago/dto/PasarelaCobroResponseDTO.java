package com.agencia.domain.dto;

import java.time.LocalDateTime;

public record PasarelaCobroResponseDTO(
        String referenciaExterna,
        String urlCheckout,
        String estadoInicial,
        LocalDateTime expiraEn,
        String payloadCrudo
) {
}

