package com.agencia.chatbot.utils;

import com.agencia.domain.dto.DisponibilidadDTO;
import com.agencia.domain.dto.PaqueteDetalleDTO;
import com.agencia.domain.dto.PaqueteResumenDTO;
import com.agencia.domain.dto.SalidaDTO;
import com.agencia.service.AgendaService;
import com.agencia.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Tool(
            name = "buscarPaquetes",
            description = """
                    Busca paquetes mediante filtros estructurados: destino, país,
                    duración máxima, presupuesto, fecha de salida o cantidad de personas.
                    
                    Úsala para búsquedas con criterios concretos. Para búsquedas basadas
                    en características, intereses o experiencias utiliza
                    buscarPaquetesPorCaracteristicas.
                    
                    Devuelve paquetes con sus IDs. No inventes IDs.
                    """
    )
    public List<PaqueteResumenDTO> buscarPaquetes(
            @ToolParam(description = "Destino o ciudad.", required = false) String destino,
            @ToolParam(description = "País del destino.", required = false) String pais,
            @ToolParam(description = "Duración máxima en días.", required = false) Integer duracionMaxima,
            @ToolParam(description = "Presupuesto máximo por persona.", required = false) BigDecimal presupuestoMaximo,
            @ToolParam(description = "Fecha de salida solicitada.", required = false) LocalDate fechaSalida,
            @ToolParam(description = "Cantidad de personas.", required = false) Integer personas
    ) {
        log.info(
                "Buscando paquetes: destino={}, pais={}, duracionMaxima={}, presupuestoMaximo={}, fechaSalida={}, personas={}",
                destino, pais, duracionMaxima, presupuestoMaximo, fechaSalida, personas
        );

        return paqueteService.buscarPaquetes(destino, pais, duracionMaxima, presupuestoMaximo, fechaSalida, personas);
    }


    @Tool(
            name = "obtenerDetallePaquete",
            description = """
                    Obtiene el detalle de un paquete turístico ya identificado.
                    
                    Úsala cuando el usuario solicite información detallada de un paquete.
                    Requiere un paqueteId obtenido previamente; no la uses para buscar paquetes.
                    """
    )
    public PaqueteDetalleDTO obtenerDetallePaquete(Long paqueteId) {

        log.info("Consultando detalle del paquete {}", paqueteId);
        return paqueteService.obtenerPublico(paqueteId.toString());
    }


    @Tool(
            name = "obtenerProximasSalidas",
            description = """
                    Obtiene las próximas salidas de un paquete identificado.
                    
                    Úsala cuando el usuario quiera saber cuándo puede viajar.
                    Si se indica una fecha, busca desde ella; si no, desde hoy.
                    Si se indican personas, considera solo salidas con cupos suficientes.
                    
                    Requiere un paqueteId obtenido previamente.
                    """
    )
    public List<SalidaDTO> obtenerProximasSalidas(
            Long paqueteId,
            @ToolParam(description = "Fecha desde la que se buscan salidas.", required = false) LocalDate desde,
            @ToolParam(description = "Cantidad de personas.", required = false) Integer personas
    ) {

        log.info("Consultando próximas salidas: paqueteId={}, desde={}, personas={}", paqueteId, desde, personas);

        return agendaService.obtenerProximasSalidas(paqueteId, desde, personas);
    }

    @Tool(
            name = "verificarDisponibilidad",
            description = """
                    Verifica la disponibilidad actual de un paquete para una fecha y
                    cantidad de personas específicas.
                    
                    Úsala para confirmar cupos y precio actual. No deduzcas estos datos
                    mediante RAG, memoria o resultados anteriores.
                    
                    Requiere paqueteId, fecha exacta y cantidad de personas.
                    """
    )
    public DisponibilidadDTO verificarDisponibilidad(Long paqueteId, LocalDate fechaSalida,
            @ToolParam(description = "Cantidad de personas.") Integer personas
    ) {

        log.info("Verificando disponibilidad: paqueteId={}, fecha={}, personas={}", paqueteId, fechaSalida, personas);

        return agendaService.verificarDisponibilidad(paqueteId, fechaSalida, personas);
    }
}