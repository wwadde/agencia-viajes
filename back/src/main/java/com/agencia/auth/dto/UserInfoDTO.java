package com.agencia.auth.dto;

import com.agencia.auth.entity.Rol;

/**
 * Información del usuario devuelta tras un login exitoso o al consultar /me.
 * No contiene tokens – la autenticación se gestiona mediante la cookie de sesión.
 */
public record UserInfoDTO(
        Long id,
        String usuario,
        String correo,
        Rol rol
) {
}

