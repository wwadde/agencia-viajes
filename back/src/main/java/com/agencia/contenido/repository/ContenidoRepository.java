package com.agencia.contenido.repository;

import com.agencia.contenido.entity.Contenido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContenidoRepository extends JpaRepository<Contenido, Long> {

    Optional<Contenido> findByClave(String clave);
}

