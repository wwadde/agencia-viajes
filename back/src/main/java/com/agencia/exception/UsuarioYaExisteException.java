package com.agencia.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un nombre o correo ya existente.
 * Se mapea a HTTP 409 Conflict en el {@link com.agencia.config.GlobalExceptionHandler}.
 */
public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String message) {
        super(message);
    }
}

