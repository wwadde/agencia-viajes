package com.agencia.controller.publico;

import com.agencia.domain.dto.AgendaSalidaDTO;
import com.agencia.service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agendas")
public class AgendaController {

    private final AgendaService agendaService;

    @GetMapping
    public ResponseEntity<List<AgendaSalidaDTO>> listar(@RequestParam(value = "paqueteId", required = false) Long paqueteId) {
        return ResponseEntity.ok(agendaService.listarProximas(paqueteId));
    }
}

