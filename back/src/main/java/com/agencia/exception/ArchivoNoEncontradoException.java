package com.agencia.exception;

/**
 * Excepción lanzada cuando se solicita un archivo que no existe
 * en el directorio de almacenamiento.
 */
public class ArchivoNoEncontradoException extends RuntimeException {

    public ArchivoNoEncontradoException(String filename) {
        super("Archivo no encontrado: " + filename);
    }
}

