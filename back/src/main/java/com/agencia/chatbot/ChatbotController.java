package com.agencia.chatbot;


import com.agencia.domain.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;


@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Valid ChatRequest request,
                           @AuthenticationPrincipal Usuario usuario) {

        return chatService.chat(request, usuario);
    }

    @GetMapping("/conversaciones/{id}")
    public ConversacionDTO getConversation(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        return chatService.getConversation(id, usuario);
    }


}



