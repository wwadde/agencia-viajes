package com.agencia.service;

import com.agencia.repository.PaqueteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SitemapServiceTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @InjectMocks
    private SitemapService sitemapService;

    @Test
    void generarSitemapXml_debeIncluirRutasEstaticasYSlugsDisponibles() {
        ReflectionTestUtils.setField(sitemapService, "frontendUrl", "https://agencia.example.com/");
        ReflectionTestUtils.setField(sitemapService, "packagePathPrefix", "/paquetes");
        when(paqueteRepository.findPublicSlugsForSitemap()).thenReturn(List.of(
                projection("caribe-premium", LocalDateTime.of(2026, 3, 20, 10, 15)),
                projection("andes-2026", LocalDateTime.of(2026, 3, 22, 8, 0))
        ));

        String sitemap = sitemapService.generarSitemapXml();

        assertTrue(sitemap.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(sitemap.contains("<loc>https://agencia.example.com/</loc><changefreq>daily</changefreq><priority>1.0</priority>"));
        assertTrue(sitemap.contains("<loc>https://agencia.example.com/paquetes</loc><changefreq>weekly</changefreq><priority>0.9</priority>"));
        assertTrue(sitemap.contains("<loc>https://agencia.example.com/paquetes/caribe-premium</loc><lastmod>2026-03-20</lastmod><changefreq>weekly</changefreq><priority>0.9</priority>"));
        assertTrue(sitemap.contains("<loc>https://agencia.example.com/paquetes/andes-2026</loc><lastmod>2026-03-22</lastmod><changefreq>weekly</changefreq><priority>0.9</priority>"));
    }

    @Test
    void generarSitemapXml_debeIgnorarSlugsVaciosYEvitarDuplicados() {
        ReflectionTestUtils.setField(sitemapService, "frontendUrl", "https://agencia.example.com");
        ReflectionTestUtils.setField(sitemapService, "packagePathPrefix", "paquetes");
        when(paqueteRepository.findPublicSlugsForSitemap()).thenReturn(List.of(
                projection("", LocalDateTime.now()),
                projection("   ", LocalDateTime.now()),
                projection("  caribe  ", LocalDateTime.of(2026, 3, 1, 12, 0)),
                projection("caribe", LocalDateTime.of(2026, 3, 5, 9, 30))
        ));

        String sitemap = sitemapService.generarSitemapXml();

        int aparicionesCaribe = sitemap.split("https://agencia.example.com/paquetes/caribe", -1).length - 1;
        assertEquals(1, aparicionesCaribe);
        assertTrue(sitemap.contains("<changefreq>weekly</changefreq><priority>0.9</priority>"));
    }

    private PaqueteRepository.PaqueteSitemapProjection projection(String slug, LocalDateTime ultimaModificacion) {
        return new PaqueteRepository.PaqueteSitemapProjection() {
            @Override
            public String getSlug() {
                return slug;
            }

            @Override
            public LocalDateTime getUltimaModificacion() {
                return ultimaModificacion;
            }
        };
    }
}

