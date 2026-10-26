package com.agencia.auth.mapper;

import com.agencia.auth.dto.RegistroUsuarioDTO;
import com.agencia.auth.dto.UserInfoDTO;
import com.agencia.auth.entity.Usuario;
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
