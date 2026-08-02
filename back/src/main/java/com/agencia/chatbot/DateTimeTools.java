package com.agencia.chatbot;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTools {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(name = "buscarFechaHoraActual", description = "Busca la fecha y hora actual en formato 'yyyy-MM-dd HH:mm:ss'")
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

}
