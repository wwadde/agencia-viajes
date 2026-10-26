package com.agencia.paquete.controller;

import com.agencia.paquete.dto.PaqueteDetalleDTO;
import com.agencia.paquete.dto.PaqueteResumenDTO;
import com.agencia.paquete.dto.PaqueteUpsertDTO;
import com.agencia.paquete.service.PaqueteService;
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
@RequestMapping("/api/admin/paquetes")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminPaqueteController {

    private final PaqueteService paqueteService;

    @GetMapping
    public ResponseEntity<List<PaqueteResumenDTO>> listar() {
        return ResponseEntity.ok(paqueteService.listarAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteDetalleDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.obtenerAdmin(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid PaqueteUpsertDTO dto) {
        Long id = paqueteService.crear(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody @Valid PaqueteUpsertDTO dto) {
        paqueteService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

