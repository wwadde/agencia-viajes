package com.agencia.repository;

import com.agencia.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    /** Verifica si ya existe un usuario con ese nombre de usuario (case-sensitive). */
    boolean existsByUsuario(String usuario);

    /** Verifica si ya existe un usuario con ese correo electrónico. */
    boolean existsByCorreo(String correo);
}
