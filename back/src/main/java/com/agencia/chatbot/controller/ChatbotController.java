package com.agencia.chatbot.controller;


import com.agencia.auth.entity.Usuario;
import com.agencia.chatbot.dto.ChatRequest;
import com.agencia.chatbot.dto.ConversacionDTO;
import com.agencia.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatService;
    private final ChatMemory chatMemory;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid ChatRequest request,
                           @AuthenticationPrincipal Usuario usuario) {

        return chatService.chat(request, usuario);
    }

    @GetMapping("/conversaciones/{id}")
    public ConversacionDTO getConversation(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        return chatService.getConversation(id, usuario);
    }

    @GetMapping("/conversaciones")
    public Page<ConversacionDTO> conversaciones(
            @AuthenticationPrincipal Usuario usuario, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return chatService.getConversaciones(usuario.getId(), page, size);
    }

    @GetMapping("/mostrarChatMemory")
    public String mostrarChatMemory(@RequestParam UUID conversacionId) {
        if (chatMemory != null) {
            List<Message> mensajes = chatMemory.get(conversacionId.toString());
            return mensajes.toString();
        } else {
            return "No hay chatMemory disponible.";
        }
    }



}



