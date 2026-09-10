package com.agencia.chatbot.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolHistorial {
    private Long paqueteId;
    private String paqueteNombre;
    private Set<ToolHistorialSalida> salidas = new HashSet<>();

}