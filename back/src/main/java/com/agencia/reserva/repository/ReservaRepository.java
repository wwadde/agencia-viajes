package com.agencia.reserva.repository;

import com.agencia.reserva.entity.Reserva;
import com.agencia.reserva.entity.ReservaEstado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @EntityGraph(attributePaths = {"usuario", "salida", "salida.paquete"})
    List<Reserva> findByUsuarioIdOrderByFechaReservaDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "salida", "salida.paquete"})
    Optional<Reserva> findByIdAndUsuarioId(Long id, Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "salida", "salida.paquete"})
    List<Reserva> findAllByOrderByFechaReservaDesc();

    @EntityGraph(attributePaths = {"usuario", "salida", "salida.paquete"})
    List<Reserva> findByEstadoOrderByFechaReservaDesc(ReservaEstado estado);

    @EntityGraph(attributePaths = {"usuario", "salida", "salida.paquete"})
    @Query("""
            SELECT r
            FROM Reserva r
            WHERE r.usuario.id = :usuarioId
              AND (:estado IS NULL OR r.estado = :estado)
            ORDER BY r.fechaReserva DESC
            """)
    List<Reserva> filtrarReservasPorUsuario(ReservaEstado estado, Long usuarioId, Pageable pageable);
}

