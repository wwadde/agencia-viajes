package com.agencia.chatbot.utils.tools;

import com.agencia.agenda.dto.DisponibilidadDTO;
import com.agencia.agenda.service.AgendaService;
import com.agencia.chatbot.utils.ContextoToolExtraccion;
import com.agencia.destino.dto.DestinoDTO;
import com.agencia.paquete.dto.PaqueteResumenDTO;
import com.agencia.paquete.dto.SalidaDTO;
import com.agencia.paquete.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaqueteTools {

    private final PaqueteService paqueteService;
    private final AgendaService agendaService;

    @Tool(description = """
                    Busca paquetes mediante filtros estructurados
                    
                    Esta herramienta sirve para encontrar paquetes que cumplan
                    condiciones específicas cuando el usuario ya sabe lo que quiere.
                    """)
    public List<PaqueteResumenDTO> filtrarPaquetes(
            @ToolParam(required = false) String pais,
            @ToolParam(required = false) String ciudad,
            @ToolParam(description = "Duración máxima en días.", required = false) Integer duracionMaxima,
            @ToolParam(description = "Presupuesto máximo por persona.", required = false) BigDecimal presupuestoMaximo,
            @ToolParam(description = "Fecha de salida solicitada.", required = false) LocalDate fechaSalida,
            @ToolParam(description = "Cantidad de personas.", required = false) Integer personas
    ) {
        log.info(
                "Buscando paquetes: pais={}, ciudad {}, duracionMaxima={}, presupuestoMaximo={}, fechaSalida={}, personas={}",
                pais, ciudad, duracionMaxima, presupuestoMaximo, fechaSalida, personas
        );

        return paqueteService.buscarPaquetes(pais, ciudad, duracionMaxima, presupuestoMaximo, fechaSalida, personas);
    }


//    @Tool(description = """
//                    Obtiene el detalle de un paquete turístico ya identificado.
//
//                    Úsala cuando el usuario solicite información detallada de un paquete.
//                    Requiere un paqueteId obtenido previamente; no la uses para buscar paquetes.
//                    """)
//    public PaqueteDetalleDTO obtenerDetallePaquete(Long paqueteId) {
//
//        log.info("Consultando detalle del paquete {}", paqueteId);
//        return paqueteService.obtenerPublico(paqueteId.toString());
//    }


    @Tool(description = """
                    Obtiene las próximas salidas de un paquete identificado.
                    
                    Úsala cuando el usuario quiera saber cuándo puede viajar.
                    Si se indica una fecha, busca desde ella; si no, desde hoy.
                    Si se indican personas, considera solo salidas con cupos suficientes.
                    
                    Requiere un paqueteId obtenido previamente.
                    """)
    public List<SalidaDTO> obtenerProximasSalidas(
            @ToolParam(description = "ID del paquete para el que se buscan salidas. Debe provenir como resultado de una tool anterior") Long paqueteId,
            @ToolParam(description = "Fecha desde la que se buscan salidas.", required = false) LocalDate desde,
            @ToolParam(description = "Cantidad de personas.", required = false) Integer personas,
            ToolContext toolContext) {

        log.info("Consultando próximas salidas: paqueteId={}, desde={}, personas={}", paqueteId, desde, personas);
        List<SalidaDTO> salidas = agendaService.obtenerProximasSalidas(paqueteId, desde, personas);
        ContextoToolExtraccion.guardarHistorialSalidas(toolContext, salidas, paqueteId);

        return salidas;
    }

    @Tool(description = """
                    Verifica la disponibilidad actual de un paquete para una fecha específica.
                    
                    Úsala para confirmar cupos y precio actual. No deduzcas estos datos
                    mediante RAG, memoria o resultados anteriores.
                    
                    Requiere paqueteId, y fecha de salida.
                    """)
    public DisponibilidadDTO verificarDisponibilidad(Long paqueteId, LocalDate fechaSalida) {

        log.info("Verificando disponibilidad: paqueteId={}, fecha={}", paqueteId, fechaSalida);
        return agendaService.verificarDisponibilidad(paqueteId, fechaSalida);
    }
    
    @Tool(description = """
                    Devuelve la ruta ordenada de destinos asociados a un paquete.
                    La ruta está conformada por los destinos del paquete. Requiere un paqueteId válido.
                    """)
    public List<DestinoDTO> buscarRuta(Long paqueteId) {
        log.info("Buscando ruta del paquete {}", paqueteId);
        return paqueteService.buscarRuta(paqueteId);
    }


}