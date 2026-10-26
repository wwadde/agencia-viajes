package com.agencia.imagen.dto;

/**
 * Respuesta devuelta tras subir una imagen exitosamente.
 *
 * @param referencia path relativo persistido en el servidor (ej: paquetes/banner-1234abcd.webp)
 * @param url URL pública para acceder a la imagen principal
 * @param thumbnailReferencia alias legado de la miniatura pequeña
 * @param thumbnailUrl alias legado de la miniatura pequeña
 * @param thumbnailSmallReferencia path relativo de la miniatura pequeña (480w)
 * @param thumbnailSmallUrl URL pública de la miniatura pequeña
 * @param thumbnailMediumReferencia path relativo de la miniatura mediana (960w)
 * @param thumbnailMediumUrl URL pública de la miniatura mediana
 */
public record ImagenSubidaResponse(
		String referencia,
		String url,
		String thumbnailReferencia,
		String thumbnailUrl,
		String thumbnailSmallReferencia,
		String thumbnailSmallUrl,
		String thumbnailMediumReferencia,
		String thumbnailMediumUrl
) {
}

