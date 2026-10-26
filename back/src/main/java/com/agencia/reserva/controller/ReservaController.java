package com.agencia.reserva.controller;

import com.agencia.auth.entity.Usuario;
import com.agencia.pago.dto.PagoCrearIntentoDTO;
import com.agencia.pago.dto.PagoIntentoDTO;
import com.agencia.pago.dto.PagoResumenDTO;
import com.agencia.pago.service.PagoService;
import com.agencia.reserva.dto.ReservaCrearDTO;
import com.agencia.reserva.dto.ReservaDTO;
import com.agencia.reserva.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservas")
@PreAuthorize("hasAuthority('CLIENTE')")
public class ReservaController {

    private final ReservaService reservaService;
    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> listar(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reservaService.listarMisReservas(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtener(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(reservaService.obtenerMia(id, usuario.getId()));
    }

    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody @Valid ReservaCrearDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        Long id = reservaService.crear(dto, usuario.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        reservaService.cancelarMia(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pagos")
    public ResponseEntity<List<PagoResumenDTO>> listarPagos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.listarMios(id, usuario.getId()));
    }

    @PostMapping("/{id}/pagos/intentos")
    public ResponseEntity<PagoIntentoDTO> crearIntentoPago(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid PagoCrearIntentoDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        // TODO Frontend Angular: redirigir a urlCheckout (PayU WebCheckout), manejar retorno en responseUrl y refrescar estado con polling.
        return ResponseEntity.ok(pagoService.crearIntento(id, usuario.getId(), dto));
    }
}
