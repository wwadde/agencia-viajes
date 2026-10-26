package com.agencia.service;

import com.agencia.config.mapper.DestinoMapper;
import com.agencia.config.mapper.PaqueteMapper;
import com.agencia.domain.Destino;
import com.agencia.domain.ImagenPaquete;
import com.agencia.domain.Paquete;
import com.agencia.domain.Salida;
import com.agencia.domain.dto.*;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.DestinoRepository;
import com.agencia.repository.PaqueteRepository;
import com.agencia.repository.SalidaRepository;
import com.agencia.utils.PaqueteSpecification;
import com.agencia.utils.TipoEntidadOutbox;
import com.agencia.utils.TipoEventoOutbox;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private final PaqueteRepository paqueteRepository;
    private final DestinoRepository destinoRepository;
    private final SalidaRepository salidaRepository;
    private final EventoOutboxService eventoOutboxService;
    private final PaqueteMapper paqueteMapper;
    private final DestinoMapper destinoMapper;

    @Value("${storage.public-base-url:}")
    private String storagePublicBaseUrl;

    public List<PaqueteResumenDTO> listarPublicos() {
        return paqueteRepository.findByDisponibleTrueOrderByDestacadoDescNombreAsc()
                .stream()
                .map(paqueteMapper::toResumenDTO)
                .toList();
    }

    public PaqueteDetalleDTO obtenerPublico(String idOrSlug) {
        Paquete paquete = resolvePublico(idOrSlug);
        if (!paquete.isDisponible()) {
            throw new RecursoNoEncontradoException("Paquete no encontrado");
        }
        return paqueteMapper.toDetalleDTO(paquete);
    }

    public List<PaqueteResumenDTO> listarAdmin() {
        return paqueteRepository.findAllByOrderByDestacadoDescNombreAsc()
                .stream()
                .map(paqueteMapper::toResumenDTO)
                .toList();
    }

    public PaqueteDetalleDTO obtenerAdmin(Long id) {
        return paqueteMapper.toDetalleDTO(getByIdOrThrow(id));
    }

    @Transactional
    public Long crear(PaqueteUpsertDTO dto) {
        List<Destino> destinos = resolverDestinos(dto.destinoIds());

        Paquete paquete = Paquete.builder().build();
        applyUpsert(paquete, dto, destinos, true);
        Paquete entidad = paqueteRepository.save(paquete);
        eventoOutboxService.registrar(TipoEntidadOutbox.PAQUETE, entidad.getId(), TipoEventoOutbox.CREADO);
        return entidad.getId();
    }

    @Transactional
    public void actualizar(Long id, PaqueteUpsertDTO dto) {
        Paquete paquete = getByIdOrThrow(id);
        List<Destino> destinos = resolverDestinos(dto.destinoIds());

        applyUpsert(paquete, dto, destinos, false);

        if (dto.imagenes() != null) {
            paquete.getImagenes().clear();
            paqueteRepository.saveAndFlush(paquete);
            paquete.getImagenes().addAll(normalizarImagenes(paquete, dto.imagenes()));
        }

        paqueteRepository.save(paquete);
        eventoOutboxService.registrar(TipoEntidadOutbox.PAQUETE, id, TipoEventoOutbox.ACTUALIZADO);

    }

    @Transactional
    public void eliminar(Long id) {
        eventoOutboxService.registrar(TipoEntidadOutbox.PAQUETE, id, TipoEventoOutbox.ELIMINADO);
        paqueteRepository.delete(getByIdOrThrow(id));
    }

    private void applyUpsert(Paquete paquete, PaqueteUpsertDTO dto, List<Destino> destinos, boolean syncImagenes) {
        paquete.setNombre(dto.nombre().trim());
        paquete.setSlug(resolveUniqueSlug(paquete.getId(), dto.slug(), dto.nombre()));
        paquete.setDescripcion(dto.descripcion().trim());
        paquete.setDetalle(dto.detalle());
        String textoPlano = dto.detalleTextoPlano();
        paquete.setDetalleTextoPlano(textoPlano == null || textoPlano.isBlank()
            ? extraerTextoPlano(dto.detalle()) : normalizarTextoPlano(textoPlano));
        paquete.setDuracionDias(dto.duracionDias());
        paquete.setDisponible(Boolean.TRUE.equals(dto.disponible()));
        paquete.setDestacado(Boolean.TRUE.equals(dto.destacado()));
        paquete.getDestinos().clear();
        paquete.getDestinos().addAll(destinos);
        paquete.setUltimaModificacion(LocalDateTime.now());

        if (syncImagenes && dto.imagenes() != null) {
            List<ImagenPaquete> nuevasImagenes = normalizarImagenes(paquete, dto.imagenes());
            paquete.getImagenes().clear();
            paquete.getImagenes().addAll(nuevasImagenes);
        }

    }

    private List<Destino> resolverDestinos(List<Long> destinoIds) {
        if (destinoIds.size() != new HashSet<>(destinoIds).size()) {
            throw new ReglaNegocioException("No se puede repetir un destino en la ruta");
        }
        List<Destino> destinos = destinoRepository.findAllById(destinoIds);
        Set<Long> idsUnicos = new HashSet<>(destinoIds);
        if (destinos.size() != idsUnicos.size()) {
            Set<Long> encontrados = destinos.stream().map(Destino::getId).collect(java.util.stream.Collectors.toSet());
            Long faltante = destinoIds.stream().filter(id -> !encontrados.contains(id)).findFirst().orElse(null);
            throw new RecursoNoEncontradoException("Destino no encontrado: " + faltante);
        }
        return destinoIds.stream().map(id -> destinos.stream()
                .filter(destino -> destino.getId().equals(id))
                .findFirst().orElseThrow()).toList();
    }

    private String extraerTextoPlano(JsonNode detalle) {
        if (detalle == null) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        extraerTextoPlano(detalle, texto);
        return normalizarTextoPlano(texto.toString());
    }

    private String normalizarTextoPlano(String texto) {
        return texto.lines()
            .map(String::trim)
            .filter(linea -> !linea.isBlank())
            .collect(Collectors.joining("\n"));
    }

    private void extraerTextoPlano(JsonNode nodo, StringBuilder texto) {
        if (nodo == null || nodo.isNull()) {
            return;
        }
        if (nodo.isString()) {
            if (!texto.isEmpty() && texto.charAt(texto.length() - 1) != '\n') {
                texto.append(' ');
            }
            texto.append(nodo.stringValue());
            return;
        }
        if (nodo.isObject() && nodo.has("content")) {
            nodo.get("content").forEach(hijo -> extraerTextoPlano(hijo, texto));
            String tipo = nodo.has("type") ? nodo.get("type").asString() : "";
            if ("paragraph".equals(tipo) || "heading".equals(tipo) || "listItem".equals(tipo)) {
                texto.append('\n');
            }
            return;
        }
        if (nodo.isArray()) {
            nodo.forEach(hijo -> extraerTextoPlano(hijo, texto));
        }
    }

    private Paquete getByIdOrThrow(Long id) {
        Paquete paquete = paqueteRepository.findDetalleBaseById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado: " + id));

        List<Salida> salidas = salidaRepository.findByPaqueteId(paquete.getId());
        paquete.setSalidas(salidas);
        return paquete;
    }

    private Paquete getBySlugOrThrow(String slug) {
        String normalizedSlug = slug == null ? null : slug.trim();
        if (normalizedSlug == null || normalizedSlug.isBlank()) {
            throw new ReglaNegocioException("Debe enviar un slug válido");
        }

        Paquete paquete = paqueteRepository.findDetalleBaseBySlug(normalizedSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado con slug: " + normalizedSlug));

        List<Salida> salidas = salidaRepository.findByPaqueteId(paquete.getId());
        paquete.setSalidas(salidas);
        return paquete;
    }

    private Paquete resolvePublico(String idOrSlug) {
        if (idOrSlug == null || idOrSlug.isBlank()) {
            throw new ReglaNegocioException("Debe enviar un identificador de paquete válido");
        }

        String value = idOrSlug.trim();
        if (value.chars().allMatch(Character::isDigit)) {
            return getByIdOrThrow(Long.parseLong(value));
        }

        return getBySlugOrThrow(value);
    }

    private List<ImagenPaquete> normalizarImagenes(Paquete paquete, List<ImagenPaqueteUpsertDTO> imagenesDTO) {
        int principalIndex = -1;
        for (int i = 0; i < imagenesDTO.size(); i++) {
            if (Boolean.TRUE.equals(imagenesDTO.get(i).principal())) {
                principalIndex = i;
                break;
            }
        }

        List<ImagenPaquete> imagenes = new ArrayList<>();
        for (int i = 0; i < imagenesDTO.size(); i++) {
            ImagenPaqueteUpsertDTO dto = imagenesDTO.get(i);
            boolean principal = principalIndex >= 0 ? i == principalIndex : i == 0;
            imagenes.add(ImagenPaquete.builder()
                    // Se persiste una referencia local (ej: paquetes/imagen.jpg), no una URL absoluta.
                    .url(dto.referencia().trim())
                    .orden(dto.orden())
                    .principal(principal)
                    .paquete(paquete)
                    .build());
        }
        return imagenes;
    }


    private String buildPublicImageUrl(String referenciaGuardada) {
        if (referenciaGuardada == null || referenciaGuardada.isBlank()) {
            return null;
        }

        String valor = referenciaGuardada.trim();
        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (storagePublicBaseUrl == null || storagePublicBaseUrl.isBlank()) {
            return valor;
        }

        String base = storagePublicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + valor;
    }

    private String resolveUniqueSlug(Long currentId, String requestedSlug, String nombre) {
        String base = slugify(requestedSlug == null || requestedSlug.isBlank() ? nombre : requestedSlug);

        List<String> usedSlugs = paqueteRepository.findUsedSlugsByBase(base, currentId);
        if (usedSlugs == null || usedSlugs.isEmpty()) {
            return base;
        }

        Set<String> used = new HashSet<>(usedSlugs);
        if (!used.contains(base)) {
            return base;
        }

        int suffix = 2;
        String candidate = base + "-" + suffix;
        while (used.contains(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    private String slugify(String raw) {
        String source = raw == null ? "" : raw;
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD)
                .replace('ñ', 'n')
                .replace('Ñ', 'N');

        normalized = COMBINING_MARKS.matcher(normalized).replaceAll("")
                .toLowerCase(Locale.ROOT)
                .trim();

        String slug = NON_ALNUM.matcher(normalized).replaceAll("-");
        slug = MULTI_DASH.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("(^-|-$)", "");

        if (slug.isBlank()) {
            return "paquete";
        }

        return slug.length() > 240 ? slug.substring(0, 240) : slug;
    }

    @Transactional(readOnly = true)
    public List<DestinoDTO> buscarRuta(Long paqueteId) {
        Paquete paquete = paqueteRepository.encontrarPaqueteConDestino(paqueteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado: " + paqueteId));

        if (paquete.getDestinos() == null || paquete.getDestinos().isEmpty()) {
            return List.of();
        }

        return paquete.getDestinos().stream()
                .map(destinoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaqueteResumenDTO> buscarPaquetes(
            String pais, String ciudad, Integer duracionMaxima,
            BigDecimal presupuestoMaximo, LocalDate fechaSalida, Integer personas
    ) {

        Specification<Paquete> specification =
                PaqueteSpecification.buscar(pais, ciudad, duracionMaxima, presupuestoMaximo, fechaSalida, personas);

        Pageable pageable = PageRequest.of(
                0,
                3,
                Sort.by(
                        Sort.Order.desc("destacado"),
                        Sort.Order.asc("nombre")
                )
        );

        return paqueteRepository.findAll(specification, pageable)
                .getContent()
                .stream()
                .map(paqueteMapper::toResumenDTO)
                .toList();
    }
}

