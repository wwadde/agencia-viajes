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

    static final int SMALL_THUMBNAIL_WIDTH = 480;
    static final int MEDIUM_THUMBNAIL_WIDTH = 960;

    private static final int WEBP_MIN_QUALITY = 45;
    private static final int WEBP_QUALITY_STEP = 8;
    private static final int WEBP_METHOD = 6;
    private static final int WEBP_DEFLATE_LEVEL = 9;
    private static final int THUMBNAIL_MAX_QUALITY = 76;
    private static final int MAX_ORIGINAL_WIDTH = 1920;
    private static final int MAX_ORIGINAL_HEIGHT = 1080;

    private final int webpQuality;

    StorageImageProcessor(int webpQuality) {
        this.webpQuality = webpQuality;
    }

    void writeOptimizedFiles(
            MultipartFile file,
            Path destination,
            Path smallThumbnailDestination,
            Path mediumThumbnailDestination,
            String referencia,
            boolean sourceAlreadyWebp
    ) {
        try {
            Files.createDirectories(destination.getParent());
            Files.createDirectories(smallThumbnailDestination.getParent());
            Files.createDirectories(mediumThumbnailDestination.getParent());
            byte[] originalBytes = file.getBytes();
            ImmutableImage image = ImmutableImage.loader().fromBytes(originalBytes);
            ImmutableImage normalizedOriginal = resizeToMaxOriginal(image);

            byte[] optimizedMain = optimizeMainImage(normalizedOriginal, originalBytes, sourceAlreadyWebp);
            Files.write(destination, optimizedMain);

            byte[] optimizedSmallThumbnail = optimizeThumbnail(resizeToWidth(normalizedOriginal, SMALL_THUMBNAIL_WIDTH));
            Files.write(smallThumbnailDestination, optimizedSmallThumbnail);

            byte[] optimizedMediumThumbnail = optimizeThumbnail(resizeToWidth(normalizedOriginal, MEDIUM_THUMBNAIL_WIDTH));
            Files.write(mediumThumbnailDestination, optimizedMediumThumbnail);

            log.info(
                    "Optimización {}: original={} bytes, principal={} bytes, thumb-small={} bytes, thumb-medium={} bytes",
                    referencia,
                    originalBytes.length,
                    optimizedMain.length,
                    optimizedSmallThumbnail.length,
                    optimizedMediumThumbnail.length
            );
        } catch (IOException | RuntimeException e) {
            throw new AlmacenamientoException("No se pudo guardar el archivo: " + referencia, e);
        }
    }

    private ImmutableImage resizeToMaxOriginal(ImmutableImage image) {
        if (image.width <= MAX_ORIGINAL_WIDTH && image.height <= MAX_ORIGINAL_HEIGHT) {
            return image;
        }
        return image.scaleToWidth(MAX_ORIGINAL_WIDTH, ScaleMethod.Lanczos3);
    }

    private ImmutableImage resizeToWidth(ImmutableImage image, int targetWidth) {
        if (image.width <= targetWidth) {
            return image;
        }
        return image.scaleToWidth(targetWidth, ScaleMethod.Lanczos3);
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
                best.length,
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

