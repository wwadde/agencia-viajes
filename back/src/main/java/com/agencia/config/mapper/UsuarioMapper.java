package com.agencia.config.mapper;

import com.agencia.domain.Usuario;
import com.agencia.domain.dto.RegistroUsuarioDTO;
import com.agencia.domain.dto.UserInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para conversión entre entidades de dominio y DTOs de usuario.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "password", ignore = true)
    Usuario toEntity(RegistroUsuarioDTO dto);

    @Mapping(source = "username", target = "usuario")
    UserInfoDTO toUserInfoDTO(Usuario usuario);
}
