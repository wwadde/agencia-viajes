package com.agencia.chatbot.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaqueteRagTools {

    private final VectorStore vectorStore;

    @Tool(
            name = "buscarPaquetesPorCaracteristicas",
            description = """
                    Busca paquetes mediante similitud semántica.

                    Úsala cuando el usuario describa intereses, actividades,
                    características o experiencias, por ejemplo: café, aventura,
                    naturaleza o turismo cultural.

                    No la uses para precios, fechas, cupos o disponibilidad.

                    Devuelve paquetes relevantes con sus IDs. Si no encuentra
                    resultados, no significa que el paquete no exista; considera
                    otra búsqueda si la solicitud permite filtros estructurados.

                    No inventes IDs ni información de paquetes.
                    """
    )
    public List<PaqueteBusquedaRag> buscarPaquetesPorCaracteristicas(
            @ToolParam(description = """
                    Descripción semántica de lo que busca el usuario.
                    Incluye los conceptos relevantes de su solicitud.

                    Reformula mentalmente la intención del usuario como una consulta
                    breve y descriptiva para buscar paquetes similares.
                    """) String consulta
    ) {

        log.info("Buscando paquetes mediante RAG: consulta={}", consulta);

        SearchRequest request = SearchRequest.builder()
                .query(consulta)
                .topK(3)
                .similarityThreshold(0.4)
                .build();


        return vectorStore.similaritySearch(request)
                .stream()
                .map(document -> new PaqueteBusquedaRag(
                        Long.valueOf(document.getMetadata().get("paqueteId").toString()),
                        document.getMetadata().get("destinoId").toString(),
                        document.getText(),
                        document.getScore()
                ))
                .toList();
    }

    public record PaqueteBusquedaRag(Long paqueteId, String destino, String descripcion, Double puntuacion) {
    }


}
