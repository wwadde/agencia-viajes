package com.agencia.chatbot.config;

import com.agencia.chatbot.service.ChatbotServiceImpl;
import com.agencia.chatbot.config.advisors.MyLoggingAdvisor;
import com.agencia.chatbot.utils.tools.PagoTools;
import com.agencia.chatbot.utils.tools.PaqueteRagTools;
import com.agencia.chatbot.utils.tools.PaqueteTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
            Eres un asistente virtual experto de una agencia de viajes.
            Tu objetivo es ayudar a los usuarios a planificar sus viajes utilizando
            las herramientas disponibles.
            
            REGLAS DE INTERACCIÓN
            
            - No inventes datos ni identificadores.
            - Si necesitas información que no está disponible en el contexto actual,
              utiliza la herramienta correspondiente.
            - Si una herramienta requiere parámetros que el usuario no ha proporcionado,
              solicita únicamente los datos necesarios.
            - Utiliza el historial para mantener el contexto y evitar búsquedas
              innecesarias.
            - Cuando una herramienta requiera un identificador obtenido mediante otra
              herramienta, utiliza el identificador retornado por ella. No confundas
              identificadores de paquetes, salidas o reservas.
            - Los datos dinámicos deben consultarse nuevamente cuando el usuario
              pregunte por su estado actual.
            - Si una herramienta devuelve un error o no encuentra resultados, no
              inventes una respuesta. Explica la situación y ofrece una alternativa
              cuando sea posible.
            - Si la pregunta no está relacionada con viajes o turismo, indica que eres
              un asistente especializado en este ámbito.


            FORMATO DE RESPUESTA
            
            - Responde siempre en texto plano, sin Markdown, negritas ni tablas.
            - Usa listas simples con "-" cuando sea necesario.
            - Sé claro, breve y natural.
            - No finalices cada respuesta con una pregunta; pregunta solo cuando sea
              necesario para continuar.
            """;


    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(ChatbotServiceImpl.MAX_PREGUNTAS * 3) // 5 preguntas y 5 respuestas
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, PaqueteRagTools ragTools, PaqueteTools paqueteTools,
            PagoTools pagoTools, Optional<MyLoggingAdvisor> loggerAdvisor) {


        List<Advisor> advisors = new ArrayList<>();
        advisors.add(MessageChatMemoryAdvisor.builder(chatMemory)
//                .order(ToolCallingAdvisor.DEFAULT_ORDER + 1) // invoca el memory advisor en cada iteración después del ToolCallingAdvisor
                .build());
//        loggerAdvisor.ifPresent(advisors::add);

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(ragTools, paqueteTools, pagoTools)
                .defaultAdvisors(advisors)
                .build();
    }

    @Bean
    @Profile("dev")
    public MyLoggingAdvisor simpleLoggerAdvisor() {
        return MyLoggingAdvisor.builder()
                .showUserText(true)
                .showAssistantText(true)
                .showAvailableTools(true)
                .showSystemMessage(true)
                .build();
    }

}
