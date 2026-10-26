package com.agencia.repository;

import com.agencia.domain.Salida;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalidaRepository extends JpaRepository<Salida, Long> {

    @EntityGraph(attributePaths = {"paquete"})
    List<Salida> findByFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(LocalDate fechaDesde);

    @EntityGraph(attributePaths = {"paquete"})
    List<Salida> findByPaqueteIdAndFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(Long paqueteId, LocalDate fechaDesde);

    @EntityGraph(attributePaths = {"paquete"})
    List<Salida> findAllByOrderByFechaSalidaAsc();

    @EntityGraph(attributePaths = {"paquete"})
    List<Salida> findByPaqueteIdOrderByFechaSalidaAsc(Long paqueteId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Salida s where s.id = :id")
    Optional<Salida> findByIdForUpdate(Long id);

    @Query("""
        select s
        from Salida s
        where s.paquete.id = :paqueteId
        order by s.fechaSalida
    """)
    List<Salida> findByPaqueteId(Long paqueteId);

    @Query("""
        SELECT s
        FROM Salida s
        WHERE s.paquete.id = :paqueteId
          AND s.fechaSalida >= :desde
          AND s.cuposDisponibles >= :personas
        ORDER BY s.fechaSalida ASC
        """)
    List<Salida> buscarProximasSalidas(@Param("paqueteId") Long paqueteId, @Param("desde") LocalDate desde, @Param("personas") Integer personas, Pageable pageable);

    @Query("""
    SELECT s
    FROM Salida s
    JOIN FETCH s.paquete p
    WHERE p.id = :paqueteId
      AND p.disponible = true
      AND s.fechaSalida <= :fechaSalida
    """)
    Optional<Salida> buscarSalida(@Param("paqueteId") Long paqueteId, @Param("fechaSalida") LocalDate fechaSalida);
}
