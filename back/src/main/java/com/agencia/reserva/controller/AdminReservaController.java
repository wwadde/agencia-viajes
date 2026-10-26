package com.agencia.reserva.controller;

import com.agencia.pago.dto.PagoResumenDTO;
import com.agencia.pago.service.PagoService;
import com.agencia.reserva.dto.ReservaDTO;
import com.agencia.reserva.dto.ReservaEstadoUpdateDTO;
import com.agencia.reserva.entity.ReservaEstado;
import com.agencia.reserva.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservas")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminReservaController {

    private final ReservaService reservaService;
    private final PagoService pagoService;

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> listar(
            @RequestParam(value = "estado", required = false) ReservaEstado estado) {
        return ResponseEntity.ok(reservaService.listarAdmin(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerAdmin(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id, @RequestBody @Valid ReservaEstadoUpdateDTO dto) {
        reservaService.actualizarEstadoAdmin(id, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pagos")
    public ResponseEntity<List<PagoResumenDTO>> listarPagos(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.listarAdminPorReserva(id));
    }
}
