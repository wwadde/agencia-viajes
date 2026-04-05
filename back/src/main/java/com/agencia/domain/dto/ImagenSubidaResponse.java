package com.agencia.domain.dto;

/**
 * Respuesta devuelta tras subir una imagen exitosamente.
 *
 * @param referencia path relativo persistido en el servidor (ej: paquetes/banner-1234abcd.webp)
 * @param url URL pública para acceder a la imagen principal
 * @param thumbnailReferencia path relativo de la miniatura optimizada
 * @param thumbnailUrl URL pública de la miniatura
 */
public record ImagenSubidaResponse(
		String referencia,
		String url,
		String thumbnailReferencia,
		String thumbnailUrl
) {
}

