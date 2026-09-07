package com.agencia.chatbot.utils;

import com.agencia.chatbot.entity.ToolHistorial;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.agencia.chatbot.service.PaqueteVectorService.PAQUETE_ID;
import static com.agencia.chatbot.service.PaqueteVectorService.PAQUETE_NOMBRE;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaqueteRagTools {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;


    @Tool(description = """
            Busca paquetes mediante similitud semántica.

            Úsala cuando el usuario describa intereses, actividades,
            características o experiencias.

            No la uses para precios, fechas, cupos o disponibilidad.
            Retorna paquetes relevantes con sus paqueteId para que puedan ser utilizados
            en herramientas posteriores si se necesita.""")
    public String buscarPaquetes(String consulta, ToolContext toolContext) {
        log.info("Buscando paquetes con RAG: {}", consulta);
        ChatClientResponse response = chatClientBuilder.clone().build().prompt()
                .advisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .similarityThreshold(0.5)
                                .topK(2)
                                .build())
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

        List<ToolHistorial> historial = ContextoConversacionTool.historial(toolContext);

        for (Document doc : documentos) {
            Long paqueteId = Long.parseLong(doc.getMetadata().get(PAQUETE_ID).toString());
            String paqueteNombre = doc.getMetadata().get(PAQUETE_NOMBRE).toString();

            historial.add(new ToolHistorial(paqueteId, paqueteNombre));
        }

        return response.chatResponse().getResult().getOutput().getText();
    }


}
