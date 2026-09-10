package com.agencia.chatbot.config.advisors;

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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HistorialAdvisor implements BaseAdvisor {

    public static final String HISTORIAL_KEY = "historialParaContexto";

    @Override
    @SuppressWarnings("unchecked")
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Set<ToolHistorial> historial = (Set<ToolHistorial>) request.context()
                .getOrDefault(HISTORIAL_KEY, List.of());

        if (historial == null || historial.isEmpty()) {
            return request;
        }

        String resumen = historial.stream()
                .map(h -> {
                    String salidasTexto = h.getSalidas().stream()
                            .map(s -> "ID: %d, Fecha: %s".formatted(s.getSalidaId(), s.getFechaSalida()))
                            .collect(Collectors.joining(" | ")); // Separador entre salidas

                    return "paqueteID: %d, nombre: %s, salidas: [%s]".formatted(
                            h.getPaqueteId(),
                            h.getPaqueteNombre(),
                            salidasTexto
                    );
                })
                .collect(Collectors.joining("; "));

        // TODO: ya funciona pero creo que esta reemplazando todo el systemprompt, habría que ver si se puede agregar al final del system prompt en vez de reemplazarlo
        Prompt promptAumentado = request.prompt()
                .augmentSystemMessage("""
                        Estado de la conversación:
                        ------------
                        %s
                        ------------
                        Utiliza estos datos para continuar operaciones iniciadas
                        previamente. No vuelvas a solicitar al usuario información
                        que ya esté disponible en este contexto.
                        """.formatted(resumen));
        log.info("HistorialAdvisor: Se ha agregado el historial al prompt: {}", promptAumentado.getSystemMessage().getText());

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
