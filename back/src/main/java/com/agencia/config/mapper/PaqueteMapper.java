package com.agencia.config.mapper;

import com.agencia.domain.Paquete;
import com.agencia.domain.dto.PaqueteDetalleDTO;
import com.agencia.domain.dto.PaqueteResumenDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaqueteMapper {

    @Mapping(target = "destino", source = "destino.nombre")
    @Mapping(target = "ciudad", source = "destino.ciudad")
    @Mapping(target = "pais", source = "destino.pais")
    @Mapping(target = "imagenPrincipal", ignore = true)
    PaqueteResumenDTO toResumenDTO(Paquete paquete);

    @Mapping(target = "imagenes", ignore = true)
    PaqueteDetalleDTO toDetalleDTOTool(Paquete paquete);
}
