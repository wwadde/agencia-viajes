package com.agencia.config.mapper;

import com.agencia.domain.Destino;
import com.agencia.domain.dto.DestinoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DestinoMapper {


    DestinoDTO toDTO(Destino destino);

    @Mapping(target = "id", ignore = true)
    Destino toEntity(DestinoDTO destino);

    @Mapping(target = "id", ignore = true)
    void actualizar(DestinoDTO dto, @MappingTarget Destino destino);


}
