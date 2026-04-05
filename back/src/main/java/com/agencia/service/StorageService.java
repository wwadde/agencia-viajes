package com.agencia.service;

import com.agencia.exception.AlmacenamientoException;
import com.agencia.exception.ArchivoInvalidoException;
import com.agencia.exception.ArchivoNoEncontradoException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Contrato para el servicio de almacenamiento de archivos.
 *
 * <p>Las implementaciones deben validar referencias seguras (sin path traversal)
 * y pueden reemplazar archivos existentes cuando se reutiliza el mismo nombre.
 */
public interface StorageService {

    /**
     * Persiste el archivo recibido en la carpeta indicada y devuelve la referencia relativa asignada.
     *
     * @param file archivo multipart a guardar; no puede estar vacío
     * @param carpeta carpeta lógica permitida (p. ej. {@code "paquetes"}, {@code "iconos"}, {@code "landing"})
     * @return referencia relativa persistida (p. ej. {@code "paquetes/banner.jpg"})
     * @throws ArchivoInvalidoException  si el tipo o tamaño no está permitido
     * @throws AlmacenamientoException   si ocurre un error de I/O
     */
    String guardar(MultipartFile file, String carpeta);

    /**
     * Elimina el archivo indicado del almacenamiento.
     *
     * @param referencia referencia relativa tal como fue devuelta por {@link #guardar(MultipartFile, String)}
     * @throws ArchivoNoEncontradoException si el archivo no existe
     * @throws AlmacenamientoException      si ocurre un error de I/O
     */
    void eliminar(String referencia);

    /**
     * Comprueba si un archivo existe en el almacenamiento.
     *
     * @param referencia referencia relativa a verificar
     * @return {@code true} si el archivo existe
     */
    boolean existe(String referencia);

    /**
     * Lista imágenes almacenadas para la biblioteca admin.
     *
     * @param carpeta filtro opcional por carpeta lógica permitida
     * @param busqueda filtro opcional por coincidencia parcial en nombre de archivo
     * @return listado de archivos con metadatos
     */
    List<StorageFileInfo> listar(String carpeta, String busqueda);
}
