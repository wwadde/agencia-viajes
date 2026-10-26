package com.agencia.controller.admin;

import com.agencia.domain.dto.ContenidoDTO;
import com.agencia.domain.dto.ContenidoUpsertDTO;
import com.agencia.service.ContenidoService;
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
@RequestMapping("/api/admin/contenidos")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminContenidoController {

    private final ContenidoService contenidoService;

    @GetMapping
    public ResponseEntity<List<ContenidoDTO>> listar() {
        return ResponseEntity.ok(contenidoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(contenidoService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody @Valid ContenidoUpsertDTO dto) {
        Long id = contenidoService.crear(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody @Valid ContenidoUpsertDTO dto) {
        contenidoService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        contenidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

