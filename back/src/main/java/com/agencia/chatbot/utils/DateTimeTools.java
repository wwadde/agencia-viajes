package com.agencia.chatbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class DateTimeTools {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(name = "buscarFechaHoraActual", description = "Busca la fecha y hora actual en formato 'yyyy-MM-dd HH:mm:ss'")
    public String getCurrentDateTime() {
        log.info("EJECUTANDO LA TOOL DE BUSCAR FECHA HORA ACTUAL");
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

}
