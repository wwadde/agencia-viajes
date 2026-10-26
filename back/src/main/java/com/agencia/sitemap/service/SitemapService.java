package com.agencia.sitemap.service;

import com.agencia.foro.repository.ForoPostRepository;
import com.agencia.paquete.repository.PaqueteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SitemapService {

    private static final String CHANGEFREQ_INICIO = "daily";
    private static final String CHANGEFREQ_PAQUETES = "weekly";
    private static final String CHANGEFREQ_FORO = "weekly";
    private static final String PRIORITY_INICIO = "1.0";
    private static final String PRIORITY_PAQUETES = "0.9";
    private static final String PRIORITY_FORO = "0.8";

    private final PaqueteRepository paqueteRepository;
    private final ForoPostRepository foroPostRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.seo.package-path-prefix:/paquetes}")
    private String packagePathPrefix;

    @Value("${app.seo.blog-path-prefix:/foro}")
    private String blogPathPrefix;

    public String generarSitemapXml() {
        Set<String> staticUrls = new LinkedHashSet<>();
        staticUrls.add(normalizarUrlBase(frontendUrl) + "/");
        staticUrls.add(construirUrl("/paquetes"));
        staticUrls.add(construirUrl("/foro"));

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        for (String url : staticUrls) {
            if (url.endsWith("/")) {
                appendUrl(xml, url, null, PRIORITY_INICIO, CHANGEFREQ_INICIO);
                continue;
            }

            if (url.endsWith("/foro")) {
                appendUrl(xml, url, null, PRIORITY_FORO, CHANGEFREQ_FORO);
                continue;
            }

            appendUrl(xml, url, null, PRIORITY_PAQUETES, CHANGEFREQ_PAQUETES);
        }

        Set<String> seenPackageUrls = new LinkedHashSet<>();
        List<PaqueteRepository.PaqueteSitemapProjection> paquetes = paqueteRepository.findPublicSlugsForSitemap();
        for (PaqueteRepository.PaqueteSitemapProjection paquete : paquetes) {
            String normalizedSlug = paquete.getSlug() == null ? "" : paquete.getSlug().trim();
            if (normalizedSlug.isEmpty()) {
                continue;
            }

            String url = construirUrl(packagePathPrefix + "/" + normalizedSlug);
            if (seenPackageUrls.add(url)) {
                appendUrl(xml, url, paquete.getUltimaModificacion(), PRIORITY_PAQUETES, CHANGEFREQ_PAQUETES);
            }
        }

        Set<String> seenForoUrls = new LinkedHashSet<>();
        List<ForoPostRepository.ForoPostSitemapProjection> posts = foroPostRepository.findPublicSlugsForSitemap();
        for (ForoPostRepository.ForoPostSitemapProjection post : posts) {
            String normalizedSlug = post.getSlug() == null ? "" : post.getSlug().trim();
            if (normalizedSlug.isEmpty()) {
                continue;
            }

            String url = construirUrl(blogPathPrefix + "/" + normalizedSlug);
            if (seenForoUrls.add(url)) {
                appendUrl(xml, url, post.getUltimaModificacion(), PRIORITY_FORO, CHANGEFREQ_FORO);
            }
        }

        xml.append("</urlset>");

        return xml.toString();
    }

    private void appendUrl(
            StringBuilder xml,
            String url,
            LocalDateTime ultimaModificacion,
            String priority,
            String changefreq
    ) {
        xml.append("<url><loc>")
                .append(escapeXml(url))
                .append("</loc>");
        if (ultimaModificacion != null) {
            LocalDate fecha = ultimaModificacion.toLocalDate();
            xml.append("<lastmod>")
                    .append(fecha)
                    .append("</lastmod>");
        }
        xml.append("<changefreq>")
                .append(changefreq)
                .append("</changefreq>");
        xml.append("<priority>")
                .append(priority)
                .append("</priority>");
        xml.append("</url>");
    }

    private String construirUrl(String path) {
        String base = normalizarUrlBase(frontendUrl);
        String normalizedPath = (path == null || path.isBlank()) ? "/" : path.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return base + normalizedPath;
    }

    private String normalizarUrlBase(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:4200";
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

