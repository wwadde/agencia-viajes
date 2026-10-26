package com.agencia.reserva.dto;

import com.agencia.reserva.entity.ReservaEstado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaDTO(
        Long id,
        LocalDateTime fechaReserva,
        ReservaEstado estado,
        Integer cantidadPersonas,
        BigDecimal precioUnitario,
        BigDecimal precioTotal,
        String observaciones,
        Long salidaId,
        LocalDate fechaSalida,
        Long paqueteId,
        String paqueteNombre,
        String paqueteSlug,
        Long clienteId,
        String clienteUsuario
) {
}

