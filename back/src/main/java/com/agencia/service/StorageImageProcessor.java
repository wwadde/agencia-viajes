package com.agencia.service;

import com.agencia.exception.AlmacenamientoException;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.ScaleMethod;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
final class StorageImageProcessor {

    private static final int WEBP_MIN_QUALITY = 45;
    private static final int WEBP_QUALITY_STEP = 8;
    private static final int WEBP_METHOD = 6;
    private static final int WEBP_DEFLATE_LEVEL = 9;
    private static final int THUMBNAIL_MAX_QUALITY = 76;

    private final int webpQuality;
    private final int thumbnailWidth;
    private final int thumbnailHeight;

    StorageImageProcessor(int webpQuality, int thumbnailWidth, int thumbnailHeight) {
        this.webpQuality = webpQuality;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
    }

    void writeOptimizedFiles(
            MultipartFile file,
            Path destination,
            Path thumbnailDestination,
            String referencia,
            boolean sourceAlreadyWebp
    ) {
        try {
            Files.createDirectories(destination.getParent());
            byte[] originalBytes = file.getBytes();
            ImmutableImage image = ImmutableImage.loader().fromBytes(originalBytes);

            byte[] optimizedMain = optimizeMainImage(image, originalBytes, sourceAlreadyWebp);
            Files.write(destination, optimizedMain);

            ImmutableImage thumbnail = image.max(thumbnailWidth, thumbnailHeight, ScaleMethod.Lanczos3);
            byte[] optimizedThumbnail = optimizeThumbnail(thumbnail);
            Files.write(thumbnailDestination, optimizedThumbnail);

            log.info(
                    "Optimización {}: original={} bytes, principal={} bytes, thumbnail={} bytes",
                    referencia,
                    originalBytes.length,
                    optimizedMain.length,
                    optimizedThumbnail.length
            );
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoException("No se pudo guardar el archivo: " + referencia, e);
        }
    }

    private byte[] optimizeMainImage(ImmutableImage image, byte[] originalBytes, boolean sourceAlreadyWebp) {
        byte[] optimized = optimizeForTargetSize(image, originalBytes.length);

        if (sourceAlreadyWebp && originalBytes.length <= optimized.length) {
            log.info(
                    "Se conserva WebP original porque re-encodear no mejora tamaño (original={} bytes, reencode={} bytes)",
                    originalBytes.length,
                    optimized.length
            );
            return originalBytes;
        }

        return optimized;
    }

    private byte[] optimizeForTargetSize(ImmutableImage image, int maxTargetBytes) {
        int startQuality = Math.max(normalizeQuality(webpQuality), WEBP_MIN_QUALITY);
        byte[] best = null;
        int bestQuality = startQuality;

        for (int quality = startQuality; quality >= WEBP_MIN_QUALITY; quality -= WEBP_QUALITY_STEP) {
            byte[] candidate = encodeWebp(image, quality);
            if (best == null || candidate.length < best.length) {
                best = candidate;
                bestQuality = quality;
            }

            if (candidate.length <= maxTargetBytes) {
                log.debug("WebP optimizado con q={} ({} bytes)", quality, candidate.length);
                return candidate;
            }
        }

        log.warn(
                "No se logró reducir por debajo del original (original={} bytes, mejor={} bytes, q={})",
                maxTargetBytes,
                best == null ? -1 : best.length,
                bestQuality
        );
        return best;
    }

    private byte[] optimizeThumbnail(ImmutableImage image) {
        int startQuality = Math.clamp(normalizeQuality(webpQuality), WEBP_MIN_QUALITY, THUMBNAIL_MAX_QUALITY);
        byte[] best = null;
        for (int quality = startQuality; quality >= WEBP_MIN_QUALITY; quality -= WEBP_QUALITY_STEP) {
            byte[] candidate = encodeWebp(image, quality);
            if (best == null || candidate.length < best.length) {
                best = candidate;
            }
        }
        return best;
    }

    private byte[] encodeWebp(ImmutableImage image, int quality) {
        WebpWriter writer = buildWriter(image, quality);
        try {
            return image.forWriter(writer).bytes();
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo codificar la imagen a WebP", e);
        }
    }

    private WebpWriter buildWriter(ImmutableImage image, int quality) {
        WebpWriter writer = WebpWriter.MAX_LOSSLESS_COMPRESSION
                .withQ(normalizeQuality(quality))
                .withM(WEBP_METHOD)
                .withZ(WEBP_DEFLATE_LEVEL);

        if (!image.awt().getColorModel().hasAlpha()) {
            writer = writer.withoutAlpha();
        }

        return writer;
    }

    private int normalizeQuality(int quality) {
        return Math.clamp(quality, 0, 100);
    }
}

