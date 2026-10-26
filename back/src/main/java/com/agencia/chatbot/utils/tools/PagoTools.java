package com.agencia.chatbot.utils.tools;

import com.agencia.chatbot.service.ChatbotServiceImpl;
import com.agencia.reserva.dto.ReservaCrearDTO;
import com.agencia.reserva.dto.ReservaDTO;
import com.agencia.reserva.entity.ReservaEstado;
import com.agencia.reserva.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PagoTools {

    private final ReservaService reservaService;


    @Tool(description = "Consulta los pedidos/reservas del usuario")
    public List<ReservaDTO> misPedidos(
            @ToolParam(description = "Opcional: filtrar por estado", required = false)
            ReservaEstado estado, ToolContext toolContext) {

        Long usuarioId = (Long) toolContext.getContext().get(ChatbotServiceImpl.USUARIO_ID_KEY);
        log.info("Consultando pedidos del usuario: {} con estado: {}", usuarioId, estado);
        return reservaService.filtrarMax5Reservas(estado, usuarioId);

    }


    @Tool(description = """
            Crea una reserva para una salida específica de un paquete turístico.
            
            Úsala únicamente cuando el usuario solicite realizar una reserva.
            Requiere un salidaId válido obtenido previamente de una herramienta
            y la cantidad de personas que indique el usuario.
            
            No confundas salidaId con paqueteId.
            """)
    public String crearReserva(
            Long salidaId, Integer cantidadPersonas, @ToolParam(required = false) String observaciones,
            ToolContext toolContext) {

        Long usuarioId = (Long) toolContext.getContext().get(ChatbotServiceImpl.USUARIO_ID_KEY);
        log.info("Generando reserva para el usuario: {} y salida: {}", usuarioId, salidaId);

        ReservaCrearDTO dto = new ReservaCrearDTO(salidaId, cantidadPersonas, observaciones);
        Long reservaId = reservaService.crear(dto, usuarioId);
        if (reservaId != null) {
            return "Reserva generada con éxito. Puedes proceder con el pago";
        } else {
            return "Error al generar la reserva. Confirma que los datos proporcionados sean correctos";
        }
    }

}
