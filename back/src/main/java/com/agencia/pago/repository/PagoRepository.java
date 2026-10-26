package com.agencia.pago.repository;

import com.agencia.pago.entity.Pago;
import com.agencia.pago.entity.PagoEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    List<Pago> findByReservaIdOrderByCreadoEnDesc(Long reservaId);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    Optional<Pago> findTopByReservaIdOrderByCreadoEnDesc(Long reservaId);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    Optional<Pago> findByReferenciaExterna(String referenciaExterna);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    Optional<Pago> findByIdAndReservaUsuarioId(Long id, Long usuarioId);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    Optional<Pago> findByIdempotencyKeyAndReservaId(String idempotencyKey, Long reservaId);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario"})
    List<Pago> findByEstadoOrderByCreadoEnDesc(PagoEstado estado);
}

