package com.agencia.chatbot.utils;

import com.agencia.chatbot.service.ChatbotServiceImpl;
import com.agencia.domain.ReservaEstado;
import com.agencia.domain.Usuario;
import com.agencia.domain.dto.ReservaDTO;
import com.agencia.service.ReservaService;
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

}
