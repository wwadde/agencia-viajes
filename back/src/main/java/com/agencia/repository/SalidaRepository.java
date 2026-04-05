package com.agencia.repository;

import com.agencia.domain.Salida;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

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
}
