package com.agencia.chatbot.repository;

import com.agencia.chatbot.entity.Conversacion;
import com.agencia.chatbot.entity.RolMensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepository extends JpaRepository<Conversacion, UUID> {



    @Query("SELECT c FROM Conversacion c WHERE c.id = :conversacionId AND c.usuario.id = :usuarioId")
    Optional<Conversacion> conversacionExiste(UUID conversacionId, Long usuarioId);

    @EntityGraph(attributePaths = "mensajes")
    Optional<Conversacion> findByIdAndUsuarioId(UUID id, Long usuarioId);

    @Modifying
    @Query("""
        UPDATE Conversacion c
        SET c.fechaActualizacion = :fecha, c.metadata = :metadata
        WHERE c.id = :id
        """)
    void actualizarMetadata(UUID id, LocalDateTime fecha, Map<String, Object> metadata);

    @Query("""
            SELECT count(m)
            FROM MensajeChat m
            WHERE m.conversacion.usuario.id = :usuarioId
            AND m.rolMensaje = :rolMensaje
            AND m.fechaHora >= :inicio
            """)
    long contarMensajesDelDia(Long usuarioId, LocalDateTime inicio, RolMensaje rolMensaje);

    @Query("""
            SELECT count(m)
            FROM MensajeChat m
            WHERE m.conversacion.id = :conversacionId
            AND m.rolMensaje = :rolMensaje
            """)
    long contarMensajesConversacion(UUID conversacionId, RolMensaje rolMensaje);

    @EntityGraph(attributePaths = "mensajes")
    Page<Conversacion> findByUsuarioId(Long usuarioId, Pageable pageable);
}
