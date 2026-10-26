package com.agencia.config.mapper;

import com.agencia.domain.Destino;
import com.agencia.domain.dto.DestinoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DestinoMapper {
    DestinoDTO toDTO(Destino destino);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = "normalize")
    @Mapping(target = "pais", source = "pais", qualifiedByName = "normalize")
    @Mapping(target = "ciudad", source = "ciudad", qualifiedByName = "normalize")
    @Mapping(target = "paquetes", ignore = true)
    Destino toEntity(DestinoDTO destino);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "nombre", qualifiedByName = "normalize")
    @Mapping(target = "pais", source = "pais", qualifiedByName = "normalize")
    @Mapping(target = "ciudad", source = "ciudad", qualifiedByName = "normalize")
    @Mapping(target = "paquetes", ignore = true)
    void actualizar(DestinoDTO dto, @MappingTarget Destino destino);

    @Named("normalize")
    default String normalize(String value) {
        return value == null ? null : value.trim();
    }
}