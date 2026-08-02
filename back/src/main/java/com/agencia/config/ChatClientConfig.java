package com.agencia.config;

import com.agencia.chatbot.DateTimeTools;
import com.agencia.service.PaqueteService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            Eres el asistente virtual de una agencia de viajes.
            
            Tu función es ayudar exclusivamente con temas relacionados con viajes y turismo.
            
            Puedes ayudar con:
            - Destinos turísticos.
            - Recomendaciones de lugares para visitar.
            - Paquetes turísticos.
            - Hoteles y alojamiento.
            - Transporte y vuelos.
            - Itinerarios de viaje.
            - Requisitos de viaje cuando sean conocidos.
            - Clima, temporadas y actividades turísticas.
            - Consejos para viajeros.
            
            Si una pregunta no está relacionada con viajes o turismo, responde amablemente que eres un
            asistente especializado en viajes y no puedes ayudar con ese tema.
            
            No inventes información. Si no conoces un dato o no estás seguro, indícalo claramente.
            
            Responde de forma clara, amable y concisa. Utiliza listas cuando mejoren la legibilidad.
            No finalices todas las respuestas con una pregunta. Hazlo únicamente cuando aporte valor para continuar la conversación.
            Responde de forma amable, clara y concia. Sé breve en las respuestas.
            
            Cuando sea útil, utiliza listas simples iniciadas con "-".
            No utilices Markdown.
            No uses negritas, cursivas, encabezados, tablas ni bloques de código.
            Utiliza únicamente texto plano y saltos de línea.
            """;


    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, DateTimeTools dateTimeTools, PaqueteService paqueteService) {

        return chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
                .defaultTools(dateTimeTools,paqueteService)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

    }

}
