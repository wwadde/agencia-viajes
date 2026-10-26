package com.agencia.controller.publico;

import com.agencia.domain.dto.PaqueteDetalleDTO;
import com.agencia.domain.dto.PaqueteResumenDTO;
import com.agencia.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/paquetes")
public class PaqueteController {

    private final PaqueteService paqueteService;

    @GetMapping
    public ResponseEntity<List<PaqueteResumenDTO>> getPaquetes() {
        return ResponseEntity.ok(paqueteService.listarPublicos());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<PaqueteDetalleDTO> getPaqueteDetalle(@PathVariable String idOrSlug) {
        return ResponseEntity.ok(paqueteService.obtenerPublico(idOrSlug));
    }
}
