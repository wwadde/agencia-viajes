package com.agencia.destino.controller;

import com.agencia.destino.dto.DestinoDTO;
import com.agencia.destino.service.DestinoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/destinos")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminDestinoController {

    private final DestinoService destinoService;

    @GetMapping
    public ResponseEntity<List<DestinoDTO>> listar() {
        return ResponseEntity.ok(destinoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DestinoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(destinoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid DestinoDTO dto) {
        Long id = destinoService.crear(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody @Valid DestinoDTO dto) {
        destinoService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        destinoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

