package com.agencia.repository;

import com.agencia.domain.Reserva;
import com.agencia.domain.ReservaEstado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

