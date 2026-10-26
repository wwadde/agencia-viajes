package com.agencia.foro.controller;

import com.agencia.auth.entity.Usuario;
import com.agencia.foro.dto.ForoComentarioCrearDTO;
import com.agencia.foro.dto.ForoComentarioDTO;
import com.agencia.foro.dto.ForoPostDetalleDTO;
import com.agencia.foro.dto.ForoPostPageDTO;
import com.agencia.foro.dto.ForoReaccionDTO;
import com.agencia.foro.service.ForoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/foro/posts")
public class ForoController {

    private final ForoService foroService;

    @GetMapping
    public ResponseEntity<ForoPostPageDTO> listarPublicos(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @AuthenticationPrincipal Usuario usuario
    ) {
        Long usuarioId = usuario != null ? usuario.getId() : null;
        return ResponseEntity.ok(foroService.listarPublicos(query, page, size, usuarioId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ForoPostDetalleDTO> obtenerPublico(
            @PathVariable String slug,
            @AuthenticationPrincipal Usuario usuario
    ) {
        Long usuarioId = usuario != null ? usuario.getId() : null;
        return ResponseEntity.ok(foroService.obtenerPublico(slug, usuarioId));
    }

    @PostMapping("/{slug}/comentarios")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<ForoComentarioDTO> comentar(
            @PathVariable String slug,
            @RequestBody @Valid ForoComentarioCrearDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        ForoComentarioDTO comentario = foroService.crearComentario(slug, dto, usuario.getId());
        return ResponseEntity.status(201).body(comentario);
    }

    @PostMapping("/{slug}/likes/toggle")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<ForoReaccionDTO> toggleLikePost(
            @PathVariable String slug,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(foroService.toggleLikePost(slug, usuario.getId()));
    }

    @PostMapping("/{slug}/comentarios/{comentarioId}/likes/toggle")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<ForoReaccionDTO> toggleLikeComentario(
            @PathVariable String slug,
            @PathVariable Long comentarioId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(foroService.toggleLikeComentario(slug, comentarioId, usuario.getId()));
    }
}
