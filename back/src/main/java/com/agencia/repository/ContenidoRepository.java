package com.agencia.repository;

import com.agencia.domain.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    Optional<Contenido> findByClave(String clave);
}

