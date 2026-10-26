package com.agencia.reserva.mapper;

import com.agencia.reserva.dto.ReservaDTO;
import com.agencia.reserva.entity.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper {

    @Mapping(target = "salidaId", source = "salida.id")
    @Mapping(target = "fechaSalida", source = "salida.fechaSalida")
    @Mapping(target = "paqueteId", source = "salida.paquete.id")
    @Mapping(target = "paqueteNombre", source = "salida.paquete.nombre")
    @Mapping(target = "paqueteSlug", source = "salida.paquete.slug")
    @Mapping(target = "clienteId", source = "usuario.id")
    @Mapping(target = "clienteUsuario", source = "usuario.username")
    ReservaDTO toDTO(Reserva reserva);
}
