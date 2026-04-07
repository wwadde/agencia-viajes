package com.agencia.service;

import com.agencia.domain.Destino;
import com.agencia.domain.ImagenPaquete;
import com.agencia.domain.Paquete;
import com.agencia.domain.dto.DestinoDTO;
import com.agencia.domain.dto.ImagenPaqueteDTO;
import com.agencia.domain.dto.ImagenPaqueteUpsertDTO;
import com.agencia.domain.dto.PaqueteDetalleDTO;
import com.agencia.domain.dto.PaqueteResumenDTO;
import com.agencia.domain.dto.PaqueteUpsertDTO;
import com.agencia.domain.dto.SalidaDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.DestinoRepository;
import com.agencia.repository.PaqueteRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaqueteService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private final PaqueteRepository paqueteRepository;
    private final DestinoRepository destinoRepository;

    @Value("${storage.public-base-url:}")
    private String storagePublicBaseUrl;

    public List<PaqueteResumenDTO> listarPublicos() {
        return paqueteRepository.findByDisponibleTrueOrderByDestacadoDescNombreAsc()
                .stream()
                .map(this::toResumen)
                .toList();
    }

    public PaqueteDetalleDTO obtenerPublico(String idOrSlug) {
        Paquete paquete = resolvePublico(idOrSlug);
        if (!paquete.isDisponible()) {
            throw new RecursoNoEncontradoException("Paquete no encontrado");
        }
        return toDetalle(paquete);
    }

    public List<PaqueteResumenDTO> listarAdmin() {
        return paqueteRepository.findAllByOrderByDestacadoDescNombreAsc()
                .stream()
                .map(this::toResumen)
                .toList();
    }

    public PaqueteDetalleDTO obtenerAdmin(Long id) {
        return toDetalle(getByIdOrThrow(id));
    }

    @Transactional
    public Long crear(PaqueteUpsertDTO dto) {
        Destino destino = destinoRepository.findById(dto.destinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Destino no encontrado: " + dto.destinoId()));

        Paquete paquete = Paquete.builder().build();
        applyUpsert(paquete, dto, destino, true);
        return paqueteRepository.save(paquete).getId();
    }

    @Transactional
    public void actualizar(Long id, PaqueteUpsertDTO dto) {
        Paquete paquete = getByIdOrThrow(id);
        Destino destino = destinoRepository.findById(dto.destinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Destino no encontrado: " + dto.destinoId()));

        applyUpsert(paquete, dto, destino, false);

        if (dto.imagenes() != null) {
            paquete.getImagenes().clear();
            paqueteRepository.saveAndFlush(paquete);
            paquete.getImagenes().addAll(normalizarImagenes(paquete, dto.imagenes()));
        }

        paqueteRepository.save(paquete);
    }

    @Transactional
    public void eliminar(Long id) {
        paqueteRepository.delete(getByIdOrThrow(id));
    }

    private void applyUpsert(Paquete paquete, PaqueteUpsertDTO dto, Destino destino, boolean syncImagenes) {
        paquete.setNombre(dto.nombre().trim());
        paquete.setSlug(resolveUniqueSlug(paquete.getId(), dto.slug(), dto.nombre()));
        paquete.setDescripcion(dto.descripcion().trim());
        paquete.setDetalle(dto.detalle().trim());
        paquete.setDuracionDias(dto.duracionDias());
        paquete.setDisponible(Boolean.TRUE.equals(dto.disponible()));
        paquete.setDestacado(Boolean.TRUE.equals(dto.destacado()));
        paquete.setDestino(destino);
        paquete.setUltimaModificacion(LocalDateTime.now());

        if (syncImagenes && dto.imagenes() != null) {
            List<ImagenPaquete> nuevasImagenes = normalizarImagenes(paquete, dto.imagenes());
            paquete.getImagenes().clear();
            paquete.getImagenes().addAll(nuevasImagenes);
        }

    }

    private Paquete getByIdOrThrow(Long id) {
        Paquete paquete = paqueteRepository.findDetalleBaseById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado: " + id));

        // Evita MultipleBagFetchException cargando la segunda colección en una consulta separada.
        Hibernate.initialize(paquete.getSalidas());
        return paquete;
    }

    private Paquete getBySlugOrThrow(String slug) {
        String normalizedSlug = slug == null ? null : slug.trim();
        if (normalizedSlug == null || normalizedSlug.isBlank()) {
            throw new ReglaNegocioException("Debe enviar un slug válido");
        }

        Paquete paquete = paqueteRepository.findDetalleBaseBySlug(normalizedSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado con slug: " + normalizedSlug));

        Hibernate.initialize(paquete.getSalidas());
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

    private PaqueteResumenDTO toResumen(Paquete paquete) {
        String destino = paquete.getDestino() != null ? paquete.getDestino().getNombre() : null;
        String imagenPrincipal = paquete.getImagenes().stream()
                .filter(ImagenPaquete::isPrincipal)
                .min(Comparator.comparing(i -> i.getOrden() == null ? Integer.MAX_VALUE : i.getOrden()))
                .or(() -> paquete.getImagenes().stream()
                        .min(Comparator.comparing(i -> i.getOrden() == null ? Integer.MAX_VALUE : i.getOrden())))
                .map(ImagenPaquete::getUrl)
                .map(this::buildPublicImageUrl)
                .orElse(null);
        return new PaqueteResumenDTO(
                paquete.getId(),
                paquete.getNombre(),
                paquete.getSlug(),
                paquete.getDescripcion(),
                paquete.getDuracionDias(),
                paquete.isDestacado(),
                destino,
                imagenPrincipal
        );
    }

    private PaqueteDetalleDTO toDetalle(Paquete paquete) {
        Destino destino = paquete.getDestino();
        DestinoDTO destinoDTO = destino == null
                ? null
                : new DestinoDTO(destino.getId(), destino.getNombre(), destino.getPais(), destino.getCiudad());

        List<SalidaDTO> salidas = paquete.getSalidas().stream()
                .map(s -> new SalidaDTO(s.getId(), s.getFechaSalida(), s.getPrecio(), s.getCuposDisponibles()))
                .toList();

        List<ImagenPaqueteDTO> imagenes = paquete.getImagenes().stream()
                .sorted(Comparator.comparing(i -> i.getOrden() == null ? Integer.MAX_VALUE : i.getOrden()))
                .map(i -> new ImagenPaqueteDTO(i.getId(), i.getUrl(), i.getOrden(), i.isPrincipal()))
                .toList();

        return new PaqueteDetalleDTO(
                paquete.getId(),
                paquete.getNombre(),
                paquete.getSlug(),
                paquete.getDescripcion(),
                paquete.getDetalle(),
                paquete.getDuracionDias(),
                paquete.isDisponible(),
                paquete.isDestacado(),
                destinoDTO,
                imagenes,
                salidas
        );
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
}

