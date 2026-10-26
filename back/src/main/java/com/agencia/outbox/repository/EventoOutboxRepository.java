package com.agencia.repository;

import com.agencia.domain.EventoOutbox;
import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoOutboxRepository extends JpaRepository<EventoOutbox, UUID> {
    List<EventoOutbox> findTop20ByEstadoOrderByCreadoEnAsc(EstadoEventoOutbox estado);


    @Modifying
    @Query("""
            UPDATE EventoOutbox e
            SET e.estado = :estado, e.procesadoEn = :fechaProcesado, e.intentos = e.intentos + 1
            WHERE e.id = :id
            """)
    void actualizar(UUID id, EstadoEventoOutbox estado, LocalDateTime fechaProcesado);

    @Modifying
    @Query("""
            UPDATE EventoOutbox e
            SET e.estado = :procesando, e.intentos = e.intentos + 1
            WHERE e.id = :id AND e.estado = :pendiente
            """)
    int intentarTomarEvento(UUID id, EstadoEventoOutbox pendiente, EstadoEventoOutbox procesando, LocalDateTime fechaProcesando);

    @Query("""
            SELECT e
            FROM EventoOutbox e
            WHERE e.tipoEntidad = :tipoEntidad AND e.entidadId = :entidadId AND (e.estado = :estadoPendiente OR e.estado = :estadoProcesando)
            """)
    Optional<EventoOutbox> encontrarSegunTipo(TipoEntidadOutbox tipoEntidad, Long entidadId, EstadoEventoOutbox estadoPendiente, EstadoEventoOutbox estadoProcesando);

}
