package com.agencia.config;

import com.agencia.chatbot.utils.DateTimeTools;
import com.agencia.chatbot.utils.PaqueteTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            Eres el asistente virtual de una agencia de viajes.
            
            Ayudas exclusivamente con temas relacionados con viajes y turismo.
            
            REGLAS
            
            - No inventes información. Si no conoces un dato o no estás seguro,
              indícalo claramente.
            - Utiliza las herramientas cuando necesites información actual,
              estructurada o específica de la agencia.
            - Los precios, fechas de salida, cupos y disponibilidad deben consultarse
              mediante las herramientas correspondientes. No los deduzcas mediante
              RAG ni mediante información anterior de la conversación.
            - No inventes identificadores de paquetes ni de salidas. Utiliza únicamente
              IDs obtenidos mediante herramientas.
            - Para fechas relativas como "hoy", "mañana" o "la próxima semana",
              utiliza la herramienta de fecha y hora cuando necesites conocer
              la fecha actual.
            - Utiliza RAG para información descriptiva o contextual que no requiera
              consultar el estado actual de la agencia.
            
            Si una pregunta no está relacionada con viajes o turismo, indica
            amablemente que eres un asistente especializado en viajes.
            
            Responde de forma clara, breve y natural.
            No finalices todas las respuestas con una pregunta.
            
            FORMATO
            
            Responde únicamente en texto plano.
            No utilices Markdown, negritas, cursivas, encabezados, tablas
            ni bloques de código.
            Cuando sea útil, utiliza listas simples iniciadas con "-".
            """;


    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore,
                                 DateTimeTools dateTimeTools, PaqueteTools paqueteTools) {

        return chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
                .defaultTools(dateTimeTools, paqueteTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .build();

    }

}
