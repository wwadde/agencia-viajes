package com.agencia.chatbot;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {


    @Query("SELECT c FROM Conversacion c WHERE c.id = :conversacionId AND c.usuario.id = :usuarioId")
    Optional<Conversacion> conversacionExiste(Long conversacionId, Long usuarioId);

    @EntityGraph(attributePaths = "mensajes")
    Optional<Conversacion> findByIdAndUsuarioId(Long id, Long usuarioId);

}
