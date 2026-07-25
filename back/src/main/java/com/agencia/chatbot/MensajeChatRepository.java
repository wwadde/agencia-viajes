package com.agencia.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {
}
