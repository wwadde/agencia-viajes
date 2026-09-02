package com.agencia.chatbot.utils;

import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;

public final class ContextoConversacionTool {

    public static final String CLAVE = "contextoConversacion";

    private ContextoConversacionTool() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> obtener(ToolContext toolContext) {
        if (toolContext == null) return Map.of();
        Object valor = toolContext.getContext().get(CLAVE);
        return valor instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }
}