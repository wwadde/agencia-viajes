package com.agencia.config;

import com.agencia.chatbot.entity.MensajeChat;
import com.agencia.chatbot.entity.RolMensaje;
import com.agencia.chatbot.repository.MensajeChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona la memoria del chat, cargando los mensajes si no existen de una conversacion actual
 * Actuando como una caché en local
 */
@Service
public class ChatMemoryManager {

    private final ChatMemory chatMemory;
    private final MensajeChatRepository mensajeChatRepository;

    private final Map<String, Instant> conversacionesActivas = new ConcurrentHashMap<>();

    public ChatMemoryManager(ChatMemory chatMemory, MensajeChatRepository mensajeChatRepository) {
        this.mensajeChatRepository = mensajeChatRepository;
        this.chatMemory = chatMemory;
    }


    public void prepararMemoriaConversacion(UUID conversacionId) {

        if (!conversacionesActivas.containsKey(conversacionId.toString())) {

            List<MensajeChat> mensajes = mensajeChatRepository.findMensajesConversacion(conversacionId);
            mensajes.forEach(mensaje -> {

                if (mensaje.getRolMensaje() == RolMensaje.USUARIO) {
                    chatMemory.add(conversacionId.toString(),
                            new UserMessage(mensaje.getMensaje()));

                } else {
                    chatMemory.add(conversacionId.toString(),
                            new AssistantMessage(mensaje.getMensaje()));
                }

            });

        }

        // Registra siempre la conversacion activa
        conversacionesActivas.put(conversacionId.toString(), Instant.now());

    }

    public void limpiar() {
        conversacionesActivas.clear();
    }

    public Set<String> conversacionesActivas() {
        return Set.copyOf(conversacionesActivas.keySet());
    }
}