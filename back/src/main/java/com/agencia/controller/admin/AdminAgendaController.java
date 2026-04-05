package com.agencia.controller.admin;

import com.agencia.domain.dto.AgendaSalidaDTO;
import com.agencia.domain.dto.AgendaSalidaUpsertDTO;
import com.agencia.service.AgendaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/agendas")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAgendaController {

    private final AgendaService agendaService;

    @GetMapping
    public ResponseEntity<List<AgendaSalidaDTO>> listar(
            @RequestParam(value = "paqueteId", required = false) Long paqueteId) {
        return ResponseEntity.ok(agendaService.listarAdmin(paqueteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendaSalidaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.obtenerAdmin(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid AgendaSalidaUpsertDTO dto) {
        Long id = agendaService.crear(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody @Valid AgendaSalidaUpsertDTO dto) {
        agendaService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        agendaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

