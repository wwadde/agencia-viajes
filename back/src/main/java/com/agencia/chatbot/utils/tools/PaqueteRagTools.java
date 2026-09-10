package com.agencia.chatbot.utils.tools;

import com.agencia.chatbot.utils.ContextoToolExtraccion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaqueteRagTools {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    private static final PromptTemplate RAG_PROMPT = new PromptTemplate("""
            Información de contexto abajo.
            
            ---------------------
            {context}
            ---------------------
            
            Dada la información de contexto y sin conocimiento previo, responde la consulta.
            
            Sigue estas reglas:
            
            1. Si la respuesta no está en el contexto, simplemente di que no sabes.
            2. Evita declaraciones como "Basado en el contexto..." o "La información proporcionada...".
            3. Adjunta también el paqueteId y el nombre del paquete de cada paquete relevante en la respuesta, si es posible.
            
            Consulta: {query}
            
            Respuesta:
            """);


    @Tool(description = """
            Busca paquetes mediante similitud semántica.
            
            Usa esta herramienta cuando el usuario describa intereses, actividades,
            características o experiencias.
            
            No la uses para precios, fechas, cupos o disponibilidad.
            """)
    public String buscarPaquetes(String consulta, ToolContext toolContext) {
        log.info("Buscando paquetes con RAG: {}", consulta);

        ChatClientResponse response = chatClientBuilder.clone().build().prompt()
                .advisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .similarityThreshold(0.5)
                                .topK(2)
                                .build())
                        .queryAugmenter(ContextualQueryAugmenter.builder()
                                .promptTemplate(RAG_PROMPT)
                                .build()
                        )
                        .build())
                .user(consulta)
                .call()
                .chatClientResponse();

        @SuppressWarnings("unchecked")
        List<Document> documentos = (List<Document>) response.context()
                .getOrDefault(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT, List.of());

        if (documentos == null || documentos.isEmpty()) {
            log.warn("No se recuperaron documentos del RAG para la consulta: {}", consulta);
            return response.chatResponse().getResult().getOutput().getText();
        }

        ContextoToolExtraccion.guardarHistorialPaquete(toolContext, documentos);

        return response.chatResponse().getResult().getOutput().getText();
    }


}
