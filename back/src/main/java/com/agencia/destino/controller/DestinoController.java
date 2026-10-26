package com.agencia.destino.controller;

import com.agencia.destino.dto.DestinoDTO;
import com.agencia.destino.service.DestinoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;

    @GetMapping
    public ResponseEntity<List<DestinoDTO>> listar() {
        return ResponseEntity.ok(destinoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(destinoService.obtener(id));
    }
}

