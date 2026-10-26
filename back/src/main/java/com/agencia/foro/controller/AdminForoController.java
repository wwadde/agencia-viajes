package com.agencia.controller.admin;

import com.agencia.domain.Usuario;
import com.agencia.domain.dto.ForoComentarioModeracionDTO;
import com.agencia.domain.dto.ForoPostDetalleDTO;
import com.agencia.domain.dto.ForoPostResumenDTO;
import com.agencia.domain.dto.ForoPostUpsertDTO;
import com.agencia.service.ForoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/admin/foro/posts")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminForoController {

    private final ForoService foroService;

    @GetMapping
    public ResponseEntity<List<ForoPostResumenDTO>> listar() {
        return ResponseEntity.ok(foroService.listarAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForoPostDetalleDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(foroService.obtenerAdmin(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody @Valid ForoPostUpsertDTO dto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        Long id = foroService.crear(dto, usuario.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody @Valid ForoPostUpsertDTO dto) {
        foroService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        foroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/comentarios/{comentarioId}/oculto")
    public ResponseEntity<Void> actualizarVisibilidadComentario(
            @PathVariable Long comentarioId,
            @RequestBody @Valid ForoComentarioModeracionDTO dto
    ) {
        foroService.actualizarVisibilidadComentario(comentarioId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comentarios/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Long comentarioId) {
        foroService.eliminarComentario(comentarioId);
        return ResponseEntity.noContent().build();
    }
}
