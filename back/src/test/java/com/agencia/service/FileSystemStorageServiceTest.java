package com.agencia.service;

import com.agencia.config.StorageProperties;
import com.agencia.exception.ArchivoInvalidoException;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void guardar_debeConvertirAWebpYGenerarThumbnail() throws IOException {
        FileSystemStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile("file", "banner.jpg", "image/png", createPngBytes());

        String referencia = service.guardar(file, "paquetes");

        assertEquals("paquetes/banner.webp", referencia);
        assertTrue(Files.exists(tempDir.resolve("paquetes/banner.webp")));
        assertTrue(Files.exists(tempDir.resolve("paquetes/banner_thumb.webp")));
    }

    @Test
    void guardar_debeRechazarWebpInvalidoAunqueComienceConRiff() {
        FileSystemStorageService service = createService();

        byte[] fakeRiff = new byte[]{
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x10, 0x00, 0x00, 0x00,
                0x57, 0x41, 0x56, 0x45  // WAVE (no WEBP)
        };
        MockMultipartFile file = new MockMultipartFile("file", "hero.webp", "image/webp", fakeRiff);

        assertThrows(ArchivoInvalidoException.class, () -> service.guardar(file, "paquetes"));
    }

    @Test
    void guardar_debeAceptarWebpValidoYGenerarThumbnail() throws IOException {
        FileSystemStorageService service = createService();
        MockMultipartFile file = new MockMultipartFile("file", "hero.jpg", "image/webp", createWebpBytes());

        String referencia = service.guardar(file, "paquetes");

        assertEquals("paquetes/hero.webp", referencia);
        assertTrue(Files.exists(tempDir.resolve("paquetes/hero.webp")));
        assertTrue(Files.exists(tempDir.resolve("paquetes/hero_thumb.webp")));
    }

    @Test
    void listar_debePermitirFiltrarPorCarpetaYNombre() throws IOException {
        FileSystemStorageService service = createService();

        byte[] pngBytes = createPngBytes();

        service.guardar(new MockMultipartFile("file", "banner-principal.jpg", "image/png", pngBytes), "paquetes");
        service.guardar(new MockMultipartFile("file", "itinerario.png", "image/png", pngBytes), "paquetes");
        service.guardar(new MockMultipartFile("file", "sol.webp", "image/png", pngBytes), "iconos");

        List<StorageFileInfo> todos = service.listar(null, null);
        assertEquals(3, todos.size());
        assertTrue(todos.stream().noneMatch(i -> i.nombre().contains("_thumb")));

        List<StorageFileInfo> soloPaquetes = service.listar("paquetes", null);
        assertEquals(2, soloPaquetes.size());
        assertTrue(soloPaquetes.stream().allMatch(i -> "paquetes".equals(i.carpeta())));

        List<StorageFileInfo> filtrados = service.listar("paquetes", "banner");
        assertEquals(1, filtrados.size());
        assertEquals("paquetes/banner-principal.webp", filtrados.getFirst().referencia());
    }

    @Test
    void guardar_debeNoAumentarTamanoSiEntradaYaEsWebp() throws IOException {
        FileSystemStorageService service = createService(95);
        byte[] webpOriginal = createWebpBytes(62);
        MockMultipartFile file = new MockMultipartFile("file", "hero.webp", "image/webp", webpOriginal);

        String referencia = service.guardar(file, "paquetes");

        Path optimizedPath = tempDir.resolve(referencia);
        long optimizedSize = Files.size(optimizedPath);
        assertTrue(optimizedSize <= webpOriginal.length);
    }

    private FileSystemStorageService createService() {
        return createService(82);
    }

    private FileSystemStorageService createService(int quality) {
        StorageProperties properties = new StorageProperties(
                tempDir.toString(),
                4_000_000,
                List.of("paquetes", "iconos", "landing"),
                quality,
                480,
                320,
                "_thumb"
        );
        return new FileSystemStorageService(properties);
    }

    private byte[] createPngBytes() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean written = ImageIO.write(createSampleImage(), "png", output);
        if (!written) {
            throw new IOException("No se pudo codificar imagen de prueba con formato: png");
        }
        return output.toByteArray();
    }

    private byte[] createWebpBytes() throws IOException {
        return createWebpBytes(90);
    }

    private byte[] createWebpBytes(int quality) throws IOException {
        return ImmutableImage.fromAwt(createSampleImage())
                .forWriter(WebpWriter.DEFAULT.withQ(quality))
                .bytes();
    }

    private BufferedImage createSampleImage() {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(0x2D6CDF));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(Color.WHITE);
            graphics.fillRect(120, 120, 560, 360);
        } finally {
            graphics.dispose();
        }
        return image;
    }
}

