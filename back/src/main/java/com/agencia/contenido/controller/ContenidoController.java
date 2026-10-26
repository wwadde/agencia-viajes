package com.agencia.contenido.controller;

import com.agencia.contenido.dto.ContenidoDTO;
import com.agencia.contenido.service.ContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contenidos")
public class ContenidoController {

    private final ContenidoService contenidoService;

    @GetMapping
    public ResponseEntity<List<ContenidoDTO>> listar() {
        return ResponseEntity.ok(contenidoService.listar());
    }

    @GetMapping("/{clave}")
    public ResponseEntity<ContenidoDTO> obtenerPorClave(@PathVariable String clave) {
        return ResponseEntity.ok(contenidoService.obtenerPorClave(clave));
    }
}

