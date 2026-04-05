package com.agencia.domain.dto;

import com.agencia.utils.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistroUsuarioDTO(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        String usuario,
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        String correo,
        @NotBlank(message = "La contraseña es obligatoria")
        @PasswordConstraint
        String password
) {
}
