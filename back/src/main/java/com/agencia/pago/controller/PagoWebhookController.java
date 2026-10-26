package com.agencia.pago.controller;

import com.agencia.pago.dto.PayUWebhookRequestDTO;
import com.agencia.pago.dto.WebhookAckDTO;
import com.agencia.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pagos")
public class PagoWebhookController {

    private final PagoService pagoService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/webhook/payu", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<WebhookAckDTO> webhookPayU(@RequestParam Map<String, String> formData) {
        PayUWebhookRequestDTO dto = new PayUWebhookRequestDTO(
                formData.getOrDefault("reference_sale", formData.get("referenceCode")),
                formData.getOrDefault("state_pol", formData.get("transactionState")),
                formData.getOrDefault("sign", formData.get("signature")),
                formData.getOrDefault("value", "0"),
                formData.getOrDefault("currency", "COP"),
                formData.get("transactionState"),
                formData.get("transaction_id"),
                formData.get("response_message_pol"),
                toPayload(formData)
        );

        pagoService.procesarWebhookPayU(dto);
        return ResponseEntity.ok(new WebhookAckDTO("Webhook PayU procesado"));
    }

    private String toPayload(Map<String, String> formData) {
        try {
            return objectMapper.writeValueAsString(formData);
        } catch (JacksonException ex) {
            return formData.toString();
        }
    }
}

