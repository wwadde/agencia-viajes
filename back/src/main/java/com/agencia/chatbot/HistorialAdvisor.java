package com.agencia.chatbot;

import com.agencia.chatbot.entity.ToolHistorial;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HistorialAdvisor implements BaseAdvisor {

    public static final String HISTORIAL_KEY = "historialParaContexto";

    @Override
    @SuppressWarnings("unchecked")
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        List<ToolHistorial> historial = (List<ToolHistorial>) request.context()
                .getOrDefault(HISTORIAL_KEY, List.of());

        if (historial.isEmpty()) {
            return request;
        }

        String resumen = historial.stream()
                .map(h -> "paqueteId: %d nombre: %s".formatted(h.paqueteId(), h.paqueteNombre()))
                .collect(Collectors.joining("; "));

        Prompt promptAumentado = request.prompt().augmentUserMessage("Paquetes ya identificados en esta conversación — " + resumen);
        log.info("HistorialAdvisor: Se ha agregado el historial al prompt: {}", promptAumentado.getContents());

        return request.mutate()
                .prompt(promptAumentado)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return response; // no necesitamos tocar la respuesta, solo la petición
    }

    @Override
    public int getOrder() {
        // Menor que ToolCallingAdvisor.DEFAULT_ORDER (HIGHEST_PRECEDENCE + 300):
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
