package com.agencia.imagen.exception;

/**
 * Excepción lanzada cuando el archivo subido no cumple con las restricciones
 * de formato, tamaño o tipo de contenido permitidos.
 */
public class ArchivoInvalidoException extends RuntimeException {

    public ArchivoInvalidoException(String message) {
        super(message);
    }
}

