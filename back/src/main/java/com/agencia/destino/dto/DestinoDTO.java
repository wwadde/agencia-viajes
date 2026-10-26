package com.agencia.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DestinoDTO(

        Long id,

        @NotBlank(message = "El nombre del destino es obligatorio")
        @Size(max = 160, message = "El nombre del destino no puede superar 160 caracteres")
        String nombre,

        @NotBlank(message = "El pais del destino es obligatorio")
        @Size(max = 120, message = "El pais del destino no puede superar 120 caracteres")
        String pais,

        @NotBlank(message = "La ciudad del destino es obligatoria")
        @Size(max = 120, message = "La ciudad del destino no puede superar 120 caracteres")
        String ciudad,

        Double latitud,

        Double longitud

) {
   public DestinoDTO(String nombre, String pais, String ciudad, Double latitud, Double longitud) {
       this(null, nombre, pais, ciudad, latitud, longitud);
   }
}

