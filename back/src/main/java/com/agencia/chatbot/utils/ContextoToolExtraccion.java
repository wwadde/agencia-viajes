package com.agencia.chatbot.utils;


import com.agencia.chatbot.entity.ToolHistorial;
import com.agencia.chatbot.entity.ToolHistorialSalida;
import com.agencia.paquete.dto.SalidaDTO;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agencia.chatbot.service.PaqueteVectorService.PAQUETE_ID;
import static com.agencia.chatbot.service.PaqueteVectorService.PAQUETE_NOMBRE;

public final class ContextoToolExtraccion {

    public static final String CLAVE = "contextoConversacion";

    private ContextoToolExtraccion() {
    }

    @SuppressWarnings("unchecked")
    public static Set<ToolHistorial> historial(ToolContext toolContext) {
        if (toolContext == null) {
            return Set.of();
        }
        Object valor = toolContext.getContext().get(CLAVE);
        if (valor instanceof Set<?> lista) {
            return (Set<ToolHistorial>) lista;
        }
        return new HashSet<>();
    }

    public static void guardarHistorialPaquete(ToolContext toolContext, List<Document> documentos){

        Set<ToolHistorial> historial = historial(toolContext);

        for (Document doc : documentos) {
            Long paqueteId = Long.parseLong(doc.getMetadata().get(PAQUETE_ID).toString());
            String paqueteNombre = doc.getMetadata().get(PAQUETE_NOMBRE).toString();

            historial.add(new ToolHistorial(paqueteId, paqueteNombre, new HashSet<>()));
        }

    }

    public static void guardarHistorialSalidas(ToolContext toolContext, List<SalidaDTO> salidas, Long paqueteId){
        Set<ToolHistorial> historial = historial(toolContext);

        Set<ToolHistorialSalida> nuevasSalidas = salidas.stream()
                .map(salida -> new ToolHistorialSalida(salida.id(), salida.fechaSalida()))
                .collect(Collectors.toSet());

        // Crear un nuevo historial para el paquete si no existe
        historial.stream().filter(h -> h.getPaqueteId().equals(paqueteId))
                .findFirst()
                .ifPresentOrElse(
                        h -> h.getSalidas().addAll(nuevasSalidas),
                        () -> {
                            // Crear un nuevo historial para el paquete
                            ToolHistorial nuevoHistorial = new ToolHistorial(paqueteId, "", nuevasSalidas);
                            historial.add(nuevoHistorial);
                        }
                );


    }
}