package com.agencia.chatbot.repository;

import com.agencia.chatbot.entity.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    @Query("""
            SELECT m
            FROM MensajeChat m
            WHERE m.conversacion.id = :conversacionId
            ORDER BY m.fechaHora
            """)
    List<MensajeChat> findMensajesConversacion(UUID conversacionId);

}
