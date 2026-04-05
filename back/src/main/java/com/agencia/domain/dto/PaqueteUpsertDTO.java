package com.agencia.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PaqueteUpsertDTO(
        @NotBlank(message = "El nombre del paquete es obligatorio")
        String nombre,

        @Size(max = 240, message = "El slug no puede superar 240 caracteres")
        String slug,

        @NotBlank(message = "La descripcion del paquete es obligatoria")
        @Size(max = 2000, message = "La descripcion no puede superar 2000 caracteres")
        String descripcion,

        @NotBlank(message = "El detalle del paquete es obligatorio")
        @Size(max = 5000, message = "El detalle no puede superar 5000 caracteres")
        String detalle,

        @NotNull(message = "La duracion del paquete es obligatoria")
        @Positive(message = "La duracion debe ser mayor a cero")
        Integer duracionDias,

        @NotNull(message = "Debe indicar si el paquete esta disponible")
        Boolean disponible,

        @NotNull(message = "Debe indicar si el paquete es destacado")
        Boolean destacado,

        @NotNull(message = "El destino es obligatorio")
        Long destinoId,

        @Valid
        @Size(min = 1, message = "Debe agregar al menos una imagen")
        List<ImagenPaqueteUpsertDTO> imagenes,

        @Valid
        @Size(min = 1, message = "Debe agregar al menos una salida")
        List<SalidaUpsertDTO> salidas
) {
}

