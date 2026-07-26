package com.agencia.chatbot;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversacionMapper {

    ConversacionDTO toResponse(Conversacion conversacion);

    MensajeChatDTO toResponse(MensajeChat mensaje);

}