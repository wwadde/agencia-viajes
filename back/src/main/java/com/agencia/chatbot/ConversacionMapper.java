package com.agencia.chatbot;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversacionMapper {

    ConversacionDTO toResponse(Conversacion conversacion);

    @Mapping(target = "mensajes", ignore = true)
    ConversacionDTO toResponseSinMensajes(Conversacion conversacion);


    MensajeChatDTO toResponse(MensajeChat mensaje);

}