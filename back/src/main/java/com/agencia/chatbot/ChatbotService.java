package com.agencia.chatbot;

import com.agencia.domain.Usuario;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface ChatbotService {
    SseEmitter chat(ChatRequest request, Usuario usuario);

    ConversacionDTO getConversation(UUID id, Usuario usuario);
}
