package com.agencia.chatbot.utils;

import com.agencia.chatbot.entity.ToolHistorial;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;

public final class ContextoConversacionTool {

    public static final String CLAVE = "contextoConversacion";

    private ContextoConversacionTool() {
    }

    @SuppressWarnings("unchecked")
    public static List<ToolHistorial> historial(ToolContext toolContext) {
        if (toolContext == null) {
            return List.of();
        }
        Object valor = toolContext.getContext().get(CLAVE);
        if (valor instanceof List<?> lista) {
            return (List<ToolHistorial>) lista;
        }
        return List.of();
    }
}