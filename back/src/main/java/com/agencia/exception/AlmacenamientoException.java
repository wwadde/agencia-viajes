package com.agencia.exception;

/**
 * Excepción lanzada cuando ocurre un error de I/O durante las operaciones
 * de almacenamiento de archivos (lectura, escritura, eliminación).
 */
public class AlmacenamientoException extends RuntimeException {

    public AlmacenamientoException(String message) {
        super(message);
    }

    public AlmacenamientoException(String message, Throwable cause) {
        super(message, cause);
    }
}

