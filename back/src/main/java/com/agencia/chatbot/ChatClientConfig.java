package com.agencia.chatbot;

import com.agencia.chatbot.service.ChatbotServiceImpl;
import com.agencia.chatbot.utils.PaqueteRagTools;
import com.agencia.chatbot.utils.PaqueteTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
        Eres el asistente virtual de una agencia de viajes.
        Ayudas exclusivamente con temas de viajes y turismo.
        
        Cuando no dispongas de información suficiente para responder,
        utiliza las herramientas disponibles para buscarla.

        REGLAS

        - No inventes información, identificadores, fechas, precios, cupos ni
          disponibilidad.
        - Los paqueteId utilizados en herramientas posteriores deben provenir de
          una herramienta anterior o del usuario.
        - El RAG solo sirve para descubrir paquetes mediante información descriptiva.
        - Los precios, fechas, cupos y disponibilidad deben consultarse mediante
          sus herramientas correspondientes; nunca los deduzcas del RAG, memoria
          o resultados anteriores.
        - Para fechas relativas como "hoy", "mañana" o "este viernes", utiliza
          la herramienta de fecha y hora cuando necesites conocer la fecha exacta.
        - Usa la memoria de conversación para evitar búsquedas innecesarias, pero
          vuelve a consultar cualquier dato dinámico cuando el usuario pregunte
          por su estado actual.

        Si la pregunta no está relacionada con viajes o turismo, indica que eres
        un asistente especializado en viajes.

        Responde de forma clara, breve y natural. No finalices todas las respuestas
        con una pregunta.

        Responde únicamente en texto plano, sin Markdown, tablas ni bloques de código.
        Cuando sea útil, utiliza listas simples iniciadas con "-".
        """;


    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(ChatbotServiceImpl.MAX_PREGUNTAS * 2) // 5 preguntas y 5 respuestas
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, PaqueteRagTools ragTools, PaqueteTools paqueteTools,
            Optional<SimpleLoggerAdvisor> loggerAdvisor) {


        List<Advisor> advisors = new ArrayList<>();
        advisors.add(MessageChatMemoryAdvisor.builder(chatMemory).build());
        // Solo agrega el logger en dev
        loggerAdvisor.ifPresent(advisors::add);


        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(ragTools, paqueteTools)
                .defaultAdvisors(advisors)
                .build();
    }

    @Bean
    @Profile("dev")
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

}
