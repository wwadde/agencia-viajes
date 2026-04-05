package com.agencia.exception;

/**
 * Excepción lanzada cuando el archivo subido no cumple con las restricciones
 * de formato, tamaño o tipo de contenido permitidos.
 */
public class CarpetaInvalidaException extends RuntimeException {

    public CarpetaInvalidaException(String message) {
        super(message);
    }
}

