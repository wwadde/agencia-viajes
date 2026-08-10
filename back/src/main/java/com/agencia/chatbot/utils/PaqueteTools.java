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
                    Busca paquetes turísticos disponibles en la agencia.
                    
                    Utiliza esta herramienta cuando el usuario quiera encontrar
                    paquetes aplicando filtros como destino, país, duración máxima,
                    presupuesto máximo, fecha de salida o cantidad de personas.
                    
                    Todos los filtros son opcionales.
                    
                    Si el usuario no proporciona un filtro, utiliza null.
                    
                    El presupuesto corresponde al precio máximo por persona.
                    
                    La fecha de salida indica la fecha desde la cual el usuario
                    está dispuesto a viajar.
                    
                    La cantidad de personas se utiliza para buscar salidas que
                    tengan suficientes cupos disponibles.
                    
                    Utiliza esta herramienta para búsquedas estructuradas.
                    No la utilices para preguntas generales sobre destinos o
                    descripciones turísticas.
                    """
    )
    public List<PaqueteResumenDTO> buscarPaquetes(
            String destino,
            String pais,
            Integer duracionMaxima,
            BigDecimal presupuestoMaximo,
            LocalDate fechaSalida,
            Integer personas
    ) {
        log.info("Buscando paquetes con los siguientes filtros: destino={}, pais={}, duracionMaxima={}, presupuestoMaximo={}, fechaSalida={}, personas={}", destino, pais, duracionMaxima, presupuestoMaximo, fechaSalida, personas);
        return paqueteService.buscarPaquetes(destino, pais, duracionMaxima, presupuestoMaximo, fechaSalida, personas);
    }


    @Tool(
            name = "buscarDetallePaquete",
            description = """
                    Obtiene el detalle completo de un paquete turístico específico.
                    
                    Utiliza esta herramienta cuando el usuario ya haya identificado
                    un paquete y quiera conocer información detallada sobre él.
                    
                    Para encontrar paquetes entre diferentes opciones utiliza
                    buscarPaquetes.
                    
                    El paqueteId corresponde al identificador devuelto por buscarPaquetes.
                    """
    )
    public PaqueteDetalleDTO buscarDetallePaquete(Long paqueteId) {

        log.info("Consultando detalle del paquete {}", paqueteId);

        return paqueteService.obtenerPublico(paqueteId.toString());
    }

    @Tool(
            name = "obtenerProximasSalidas",
            description = """
                    Obtiene las próximas fechas de salida disponibles de un paquete.
                    
                    Utiliza esta herramienta cuando el usuario quiera saber cuándo
                    puede viajar con un paquete específico.
                    
                    Si el usuario proporciona una cantidad de personas, solo se
                    consideran salidas que tengan suficientes cupos para todas ellas.
                    
                    Si no se proporciona una fecha desde la cual buscar, utiliza
                    la fecha actual.
                    
                    Devuelve como máximo las 3 próximas salidas disponibles.
                    """
    )
    public List<SalidaDTO> obtenerProximasSalidas(Long paqueteId, LocalDate desde, Integer personas) {

        log.info("Consultando próximas salidas: paqueteId={}, desde={}, personas={}", paqueteId, desde, personas);

        return agendaService.obtenerProximasSalidas(paqueteId, desde, personas);
    }

    @Tool(
            name = "verificarDisponibilidad",
            description = """
        Verifica la disponibilidad actual de un paquete turístico para
        una fecha específica y una cantidad determinada de personas.

        Utiliza esta herramienta cuando el usuario pregunte si puede
        viajar en una fecha concreta.

        Esta herramienta consulta los cupos actuales de la base de datos.
        No asumas disponibilidad basándote en información de RAG,
        memoria de conversación o respuestas anteriores.

        Devuelve también el precio actual por persona y los cupos
        disponibles para esa salida.
        """
    )
    public DisponibilidadDTO verificarDisponibilidad(Long paqueteId, LocalDate fechaSalida, Integer personas) {

        log.info("Verificando disponibilidad: paqueteId={}, fecha={}, personas={}", paqueteId, fechaSalida, personas);

        return agendaService.verificarDisponibilidad(paqueteId, fechaSalida, personas);
    }


}