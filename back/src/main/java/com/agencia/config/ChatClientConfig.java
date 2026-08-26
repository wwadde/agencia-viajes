package com.agencia.config;

import com.agencia.chatbot.service.ChatbotServiceImpl;
import com.agencia.chatbot.utils.PaqueteRagTools;
import com.agencia.chatbot.utils.PaqueteTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
        Eres el asistente virtual de una agencia de viajes.
        Ayudas exclusivamente con temas de viajes y turismo.

        FLUJO DE BÚSQUEDA

        - buscarPaquetes: úsala para filtros estructurados como destino, país,
          presupuesto, duración, fecha o cantidad de personas.

        - buscarPaquetesPorCaracteristicas: úsala para búsquedas semánticas basadas en
          intereses, actividades, características o experiencias, por ejemplo:
          "algo relacionado con café", "un viaje de aventura" o "algo romántico".

        - obtenerDetallePaquete: úsala cuando ya tengas identificado un paquete
          y el usuario solicite información detallada sobre él.

        - obtenerProximasSalidas: úsala cuando el usuario quiera conocer las
          próximas fechas de salida de un paquete identificado.

        - verificarDisponibilidad: úsala cuando el usuario quiera comprobar
          disponibilidad para una fecha y cantidad de personas específicas.

        Una solicitud puede requerir varias herramientas. Primero identifica el
        paquete mediante buscarPaquetes o buscarPaquetesPorCaracteristicas y después
        utiliza la herramienta necesaria para consultar sus datos.

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
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore,
                                 PaqueteRagTools paqueteRagTools, PaqueteTools paqueteTools) {




        return chatClientBuilder.defaultSystem(SYSTEM_PROMPT)
                .defaultTools(paqueteRagTools, paqueteTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                )
                .build();

    }

}
