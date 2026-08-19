package com.agencia.chatbot.service;

import com.agencia.domain.Paquete;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.PaqueteRepository;

import com.agencia.utils.TipoEventoOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaqueteVectorService {

    private final VectorStore vectorStore;
    private final PaqueteRepository paqueteRepository;

    private static final String TIPO_PAQUETE = "paquete";

    public boolean indexarPaquete(Long paqueteId, TipoEventoOutbox tipoEvento) {

        if (tipoEvento.equals(TipoEventoOutbox.ELIMINADO)) {
            eliminarDelVectorStore(paqueteId);
            return true;
        }

        if (tipoEvento.equals(TipoEventoOutbox.ACTUALIZADO)) {
            eliminarDelVectorStore(paqueteId);
        }

        Paquete paquete = paqueteRepository.encontrarPaqueteConDestino(paqueteId)
                .orElseThrow(() -> new ReglaNegocioException("Paquete no encontrado con id: " + paqueteId));


        Document document = getDocument(paquete);
        log.info("Documento con embeddings con id {}", document.getId());

        vectorStore.add(List.of(document));
        log.info("Paquete indexado correctamente: {}", paquete.getId());
        return true;
    }

    private static Document getDocument(Paquete paquete) {
        String contenido = """
                PaqueteId: %d
                
                Nombre: %s
                
                Descripción:
                %s
                
                Detalle:
                %s
                
                Destino:
                %s
                """.formatted(
                paquete.getId(),
                paquete.getNombre(),
                paquete.getDescripcion(),
                paquete.getDetalle(),
                paquete.getDestino().getNombre()
        );

        return new Document(contenido,
                Map.of(
                        "tipo", TIPO_PAQUETE,
                        "paqueteId", paquete.getId().toString(),
                        "destinoId", paquete.getDestino().getId().toString(),
                        "duracionDias", paquete.getDuracionDias().toString()
                )
        );
    }

    private void eliminarDelVectorStore(Long paqueteId) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("paqueteId"),
                new Filter.Value(paqueteId.toString())
        );
        vectorStore.delete(filterExpression);
        log.info("Paquete eliminado del vectorstore: {}", paqueteId);
    }

}
