package com.agencia.chatbot.utils;

import com.agencia.domain.ReservaEstado;
import com.agencia.domain.Usuario;
import com.agencia.domain.dto.ReservaDTO;
import com.agencia.service.ReservaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
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
            ReservaEstado estado) {

        Long usuarioId = extraerUsuarioId();
        log.info("Consultando pedidos del usuario: {} con estado: {}", usuarioId, estado);
        return reservaService.filtrarMax5Reservas(estado, usuarioId);

    }


    private Long extraerUsuarioId() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Usuario usuario) {
            return usuario.getId();
        }

        return null;
    }
}
