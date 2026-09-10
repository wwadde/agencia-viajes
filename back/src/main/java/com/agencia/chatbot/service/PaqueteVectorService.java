package com.agencia.chatbot.service;

import com.agencia.domain.Destino;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaqueteVectorService {

    private final VectorStore vectorStore;
    private final PaqueteRepository paqueteRepository;

    public static final String TIPO_PAQUETE = "paquete";
    public static final String PAQUETE_ID = "paqueteId";
    public static final String PAQUETE_NOMBRE = "paqueteNombre";

    public boolean indexarPaquete(Long paqueteId, TipoEventoOutbox tipoEvento) {

        if (tipoEvento.equals(TipoEventoOutbox.ELIMINADO)) {
            eliminarDelVectorStore(paqueteId);
            return true;
        }

        if (tipoEvento.equals(TipoEventoOutbox.ACTUALIZADO)) {
            eliminarDelVectorStore(paqueteId);
        }

        // TODO: considerar si traer el paquete con salidas y agregarlos a la metadata
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
                paquete.getDetalleTextoPlano(),
                paquete.getDestinos().stream()
                        .map(destino -> destino.getNombre() + " (" + destino.getCiudad() + ", " + destino.getPais() + ")")
                        .collect(Collectors.joining(" -> "))
        );

        return new Document(contenido,
                Map.of(
                        "tipo", TIPO_PAQUETE,
                        PAQUETE_ID, paquete.getId().toString(),
                        PAQUETE_NOMBRE, paquete.getNombre(),
                        "destinoIds", paquete.getDestinos().stream().map(destino -> destino.getId().toString()).collect(Collectors.joining(",")),
                        "destinosNombres", paquete.getDestinos().stream().map(Destino::getNombre).collect(Collectors.joining(" -> ")),
                        "duracionDias", paquete.getDuracionDias().toString()
                )
        );
    }

    private void eliminarDelVectorStore(Long paqueteId) {
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key(PAQUETE_ID),
                new Filter.Value(paqueteId.toString())
        );
        vectorStore.delete(filterExpression);
        log.info("Paquete eliminado del vectorstore: {}", paqueteId);
    }

}
