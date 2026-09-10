package com.agencia.chatbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolHistorialSalida {
    private Long salidaId;
    private LocalDate fechaSalida;

}