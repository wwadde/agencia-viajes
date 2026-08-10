package com.agencia.chatbot.utils;

import com.agencia.chatbot.dto.ConversacionDTO;
import com.agencia.chatbot.dto.MensajeChatDTO;
import com.agencia.chatbot.entity.Conversacion;
import com.agencia.chatbot.entity.MensajeChat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConversacionMapper {

    ConversacionDTO toResponse(Conversacion conversacion);

    @Mapping(target = "mensajes", ignore = true)
    ConversacionDTO toResponseSinMensajes(Conversacion conversacion);


    MensajeChatDTO toResponse(MensajeChat mensaje);

}