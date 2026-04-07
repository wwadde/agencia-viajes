package com.agencia.service;

import com.agencia.domain.ForoBloqueTipo;
import com.agencia.domain.ForoComentario;
import com.agencia.domain.ForoComentarioReaccion;
import com.agencia.domain.ForoPost;
import com.agencia.domain.ForoPostBloque;
import com.agencia.domain.ForoPostReaccion;
import com.agencia.domain.Rol;
import com.agencia.domain.Usuario;
import com.agencia.domain.dto.ForoBloqueDTO;
import com.agencia.domain.dto.ForoBloqueUpsertDTO;
import com.agencia.domain.dto.ForoComentarioCrearDTO;
import com.agencia.domain.dto.ForoComentarioModeracionDTO;
import com.agencia.domain.dto.ForoComentarioDTO;
import com.agencia.domain.dto.ForoPostPageDTO;
import com.agencia.domain.dto.ForoPostDetalleDTO;
import com.agencia.domain.dto.ForoReaccionDTO;
import com.agencia.domain.dto.ForoPostResumenDTO;
import com.agencia.domain.dto.ForoPostUpsertDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.ForoComentarioReaccionRepository;
import com.agencia.repository.ForoComentarioRepository;
import com.agencia.repository.ForoPostRepository;
import com.agencia.repository.ForoPostReaccionRepository;
import com.agencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForoService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTI_DASH = Pattern.compile("-{2,}");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    private final ForoPostRepository foroPostRepository;
    private final ForoComentarioRepository foroComentarioRepository;
    private final ForoPostReaccionRepository foroPostReaccionRepository;
    private final ForoComentarioReaccionRepository foroComentarioReaccionRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${storage.public-base-url:}")
    private String storagePublicBaseUrl;

        public ForoPostPageDTO listarPublicos(String query, Integer page, Integer size, Long usuarioId) {
        int currentPage = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null ? 9 : Math.min(Math.max(size, 1), 30);
        String normalizedQuery = query == null ? null : query.trim();
        if (normalizedQuery != null && normalizedQuery.isBlank()) {
            normalizedQuery = null;
        }

        PageRequest pageable = PageRequest.of(
            currentPage,
            pageSize,
            Sort.by(
                Sort.Order.desc("fechaPublicacion"),
                Sort.Order.desc("ultimaModificacion")
            )
        );

        Page<ForoPost> result = foroPostRepository.findPublicByQuery(normalizedQuery, pageable);
        List<ForoPost> posts = result.getContent();

        Map<Long, Long> commentsByPostId = resolveCommentTotals(posts, false);
        Map<Long, Long> postLikes = resolvePostLikeTotals(posts);
        Set<Long> likedPostIds = resolveLikedPostIds(posts, usuarioId);

        List<ForoPostResumenDTO> items = posts.stream()
            .map(post -> toResumen(
                post,
                commentsByPostId.getOrDefault(post.getId(), 0L),
                postLikes.getOrDefault(post.getId(), 0L),
                likedPostIds.contains(post.getId()),
                true
            ))
            .toList();

        return new ForoPostPageDTO(
            items,
            currentPage,
            pageSize,
            result.getTotalElements(),
            result.getTotalPages(),
            result.hasNext(),
            result.hasPrevious()
        );
    }

        public ForoPostDetalleDTO obtenerPublico(String slug, Long usuarioId) {
        ForoPost post = getBySlugOrThrow(slug);
        if (!post.isPublicado()) {
            throw new RecursoNoEncontradoException("Publicación no encontrada");
        }

        return toDetalle(post, true, false, usuarioId);
    }

    public List<ForoPostResumenDTO> listarAdmin() {
        List<ForoPost> posts = foroPostRepository.findAllByOrderByFechaPublicacionDescUltimaModificacionDesc();
        Map<Long, Long> commentsByPostId = resolveCommentTotals(posts, true);
        Map<Long, Long> postLikes = resolvePostLikeTotals(posts);

        return posts.stream()
            .map(post -> toResumen(
                post,
                commentsByPostId.getOrDefault(post.getId(), 0L),
                postLikes.getOrDefault(post.getId(), 0L),
                false,
                false
            ))
                .toList();
    }

    public ForoPostDetalleDTO obtenerAdmin(Long id) {
        return toDetalle(getByIdOrThrow(id), false, true, null);
    }

    @Transactional
    public Long crear(ForoPostUpsertDTO dto, Long autorId) {
        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + autorId));

        ForoPost post = ForoPost.builder().build();
        applyUpsert(post, dto, autor);
        return foroPostRepository.save(post).getId();
    }

    @Transactional
    public void actualizar(Long id, ForoPostUpsertDTO dto) {
        ForoPost post = getByIdOrThrow(id);
        applyUpsert(post, dto, post.getAutor());
        foroPostRepository.save(post);
    }

    @Transactional
    public void eliminar(Long id) {
        foroPostRepository.delete(getByIdOrThrow(id));
    }

    @Transactional
    public ForoComentarioDTO crearComentario(String slug, ForoComentarioCrearDTO dto, Long usuarioId) {
        ForoPost post = getBySlugOrThrow(slug);
        if (!post.isPublicado()) {
            throw new RecursoNoEncontradoException("Publicación no encontrada");
        }

        Usuario usuario = getClienteOrThrow(usuarioId);

        ForoComentario comentario = ForoComentario.builder()
                .post(post)
                .usuario(usuario)
                .contenido(dto.contenido().trim())
                .fechaCreacion(LocalDateTime.now())
                .oculto(false)
                .build();

        ForoComentario persisted = foroComentarioRepository.save(comentario);
        return toComentarioDTO(persisted, 0L, false);
    }

    @Transactional
    public void actualizarVisibilidadComentario(Long comentarioId, ForoComentarioModeracionDTO dto) {
        ForoComentario comentario = getComentarioOrThrow(comentarioId);
        comentario.setOculto(Boolean.TRUE.equals(dto.oculto()));
        foroComentarioRepository.save(comentario);
    }

    @Transactional
    public void eliminarComentario(Long comentarioId) {
        foroComentarioRepository.delete(getComentarioOrThrow(comentarioId));
    }

    @Transactional
    public ForoReaccionDTO toggleLikePost(String slug, Long usuarioId) {
        ForoPost post = getBySlugOrThrow(slug);
        if (!post.isPublicado()) {
            throw new RecursoNoEncontradoException("Publicación no encontrada");
        }

        Usuario usuario = getClienteOrThrow(usuarioId);
        boolean likedByMe;

        ForoPostReaccion existing = foroPostReaccionRepository.findByPostIdAndUsuarioId(post.getId(), usuario.getId())
                .orElse(null);
        if (existing != null) {
            foroPostReaccionRepository.delete(existing);
            likedByMe = false;
        } else {
            foroPostReaccionRepository.save(ForoPostReaccion.builder()
                    .post(post)
                    .usuario(usuario)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
            likedByMe = true;
        }

        long total = foroPostReaccionRepository.countByPostId(post.getId());
        return new ForoReaccionDTO(total, likedByMe);
    }

    @Transactional
    public ForoReaccionDTO toggleLikeComentario(String slug, Long comentarioId, Long usuarioId) {
        ForoPost post = getBySlugOrThrow(slug);
        if (!post.isPublicado()) {
            throw new RecursoNoEncontradoException("Publicación no encontrada");
        }

        ForoComentario comentario = foroComentarioRepository.findByIdAndPostId(comentarioId, post.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Comentario no encontrado"));
        if (comentario.isOculto()) {
            throw new RecursoNoEncontradoException("Comentario no encontrado");
        }

        Usuario usuario = getClienteOrThrow(usuarioId);
        boolean likedByMe;

        ForoComentarioReaccion existing = foroComentarioReaccionRepository
                .findByComentarioIdAndUsuarioId(comentario.getId(), usuario.getId())
                .orElse(null);
        if (existing != null) {
            foroComentarioReaccionRepository.delete(existing);
            likedByMe = false;
        } else {
            foroComentarioReaccionRepository.save(ForoComentarioReaccion.builder()
                    .comentario(comentario)
                    .usuario(usuario)
                    .fechaCreacion(LocalDateTime.now())
                    .build());
            likedByMe = true;
        }

        long total = foroComentarioReaccionRepository.countByComentarioId(comentario.getId());
        return new ForoReaccionDTO(total, likedByMe);
    }

    private void applyUpsert(ForoPost post, ForoPostUpsertDTO dto, Usuario autor) {
        post.setTitulo(dto.titulo().trim());
        post.setSlug(resolveUniqueSlug(post.getId(), dto.slug(), dto.titulo()));
        post.setResumen(dto.resumen().trim());
        post.setPublicado(Boolean.TRUE.equals(dto.publicado()));
        post.setUltimaModificacion(LocalDateTime.now());
        post.setAutor(autor);

        if (post.isPublicado()) {
            if (post.getFechaPublicacion() == null) {
                post.setFechaPublicacion(LocalDateTime.now());
            }
        } else {
            post.setFechaPublicacion(null);
        }

        List<ForoPostBloque> nuevosBloques = normalizarBloques(post, dto.bloques());
        post.getBloques().clear();
        post.getBloques().addAll(nuevosBloques);
    }

    private List<ForoPostBloque> normalizarBloques(ForoPost post, List<ForoBloqueUpsertDTO> bloquesDTO) {
        if (bloquesDTO == null || bloquesDTO.isEmpty()) {
            throw new ReglaNegocioException("Debe agregar al menos un bloque al post");
        }

        boolean tieneTexto = bloquesDTO.stream()
                .anyMatch(b -> b.tipo() == ForoBloqueTipo.TEXTO && b.contenido() != null && !b.contenido().isBlank());
        if (!tieneTexto) {
            throw new ReglaNegocioException("El post debe incluir al menos un bloque de texto");
        }

        List<ForoPostBloque> bloques = new ArrayList<>();
        for (int i = 0; i < bloquesDTO.size(); i++) {
            ForoBloqueUpsertDTO dto = bloquesDTO.get(i);
            String contenido = dto.contenido().trim();
            String pieImagen = dto.pieImagen() == null ? null : dto.pieImagen().trim();

            if (dto.tipo() == ForoBloqueTipo.IMAGEN && (contenido.startsWith("http://") || contenido.startsWith("https://"))) {
                throw new ReglaNegocioException("Las imágenes del foro deben guardarse como referencia local (ej: foro/imagen.webp)");
            }

            bloques.add(ForoPostBloque.builder()
                    .post(post)
                    .tipo(dto.tipo())
                    .contenido(contenido)
                    .pieImagen(pieImagen == null || pieImagen.isBlank() ? null : pieImagen)
                    .orden(dto.orden() == null ? i : dto.orden())
                    .build());
        }

        return bloques;
    }

    private ForoPost getByIdOrThrow(Long id) {
        return foroPostRepository.findDetalleBaseById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Post no encontrado: " + id));
    }

    private ForoComentario getComentarioOrThrow(Long id) {
        return foroComentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comentario no encontrado: " + id));
    }

    private Usuario getClienteOrThrow(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + userId));
        if (usuario.getRol() != Rol.CLIENTE) {
            throw new ReglaNegocioException("Solo los clientes registrados pueden realizar esta acción");
        }
        return usuario;
    }

    private ForoPost getBySlugOrThrow(String slug) {
        String normalizedSlug = slug == null ? null : slug.trim();
        if (normalizedSlug == null || normalizedSlug.isBlank()) {
            throw new ReglaNegocioException("Debe enviar un slug válido");
        }

        return foroPostRepository.findDetalleBaseBySlug(normalizedSlug)
                .orElseThrow(() -> new RecursoNoEncontradoException("Post no encontrado con slug: " + normalizedSlug));
    }

        private ForoPostResumenDTO toResumen(
            ForoPost post,
            Long totalComentarios,
            Long totalLikes,
            boolean likedByMe,
            boolean mapPublicImageUrl
        ) {
        String imagenPortada = post.getBloques().stream()
                .filter(b -> b.getTipo() == ForoBloqueTipo.IMAGEN)
                .min(Comparator.comparing(b -> b.getOrden() == null ? Integer.MAX_VALUE : b.getOrden()))
                .map(ForoPostBloque::getContenido)
                .map(ref -> mapPublicImageUrl ? buildPublicImageUrl(ref) : ref)
                .orElse(null);

        return new ForoPostResumenDTO(
                post.getId(),
                post.getTitulo(),
                post.getSlug(),
                post.getResumen(),
                imagenPortada,
                post.getFechaPublicacion(),
                totalComentarios,
                totalLikes,
                likedByMe
        );
    }

            private ForoPostDetalleDTO toDetalle(ForoPost post, boolean mapPublicImageUrl, boolean includeHiddenComments, Long usuarioId) {
        List<ForoBloqueDTO> bloques = post.getBloques().stream()
                .sorted(Comparator.comparing(b -> b.getOrden() == null ? Integer.MAX_VALUE : b.getOrden()))
                .map(block -> toBloqueDTO(block, mapPublicImageUrl))
                .toList();

            List<ForoComentario> comentarioEntities = includeHiddenComments
                ? foroComentarioRepository.findByPostIdOrderByFechaCreacionAscIdAsc(post.getId())
                : foroComentarioRepository.findByPostIdAndOcultoFalseOrderByFechaCreacionAscIdAsc(post.getId());

            Map<Long, Long> commentLikes = resolveCommentLikeTotals(comentarioEntities);
            Set<Long> likedCommentIds = resolveLikedCommentIds(comentarioEntities, usuarioId);

        List<ForoComentarioDTO> comentarios = comentarioEntities.stream()
                .map(c -> toComentarioDTO(
                    c,
                    commentLikes.getOrDefault(c.getId(), 0L),
                    likedCommentIds.contains(c.getId())
                ))
                .toList();

            Long totalLikes = foroPostReaccionRepository.countByPostId(post.getId());
            boolean likedByMe = usuarioId != null
                && !resolveLikedPostIds(List.of(post), usuarioId).isEmpty();

        return new ForoPostDetalleDTO(
                post.getId(),
                post.getTitulo(),
                post.getSlug(),
                post.getResumen(),
                post.isPublicado(),
                post.getFechaPublicacion(),
                post.getUltimaModificacion(),
                post.getAutor() != null ? post.getAutor().getUsuario() : null,
                totalLikes,
                likedByMe,
                bloques,
                comentarios
        );
    }

    private ForoBloqueDTO toBloqueDTO(ForoPostBloque block, boolean mapPublicImageUrl) {
        String contenido = block.getContenido();
        if (mapPublicImageUrl && block.getTipo() == ForoBloqueTipo.IMAGEN) {
            contenido = buildPublicImageUrl(contenido);
        }

        return new ForoBloqueDTO(
                block.getId(),
                block.getTipo(),
                contenido,
                block.getPieImagen(),
                block.getOrden()
        );
    }

    private ForoComentarioDTO toComentarioDTO(ForoComentario comentario, Long totalLikes, boolean likedByMe) {
        return new ForoComentarioDTO(
                comentario.getId(),
                comentario.getUsuario() != null ? comentario.getUsuario().getUsername() : null,
                comentario.getContenido(),
                comentario.getFechaCreacion(),
                comentario.isOculto(),
                totalLikes,
                likedByMe
        );
    }

    private Map<Long, Long> resolveCommentTotals(List<ForoPost> posts, boolean includeHidden) {
        if (posts == null || posts.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = posts.stream().map(ForoPost::getId).toList();
        Map<Long, Long> result = new HashMap<>();
        List<ForoComentarioRepository.ForoComentarioCountProjection> source = includeHidden
                ? foroComentarioRepository.countAllByPostIds(ids)
                : foroComentarioRepository.countVisibleByPostIds(ids);

        source
                .forEach(row -> result.put(row.getPostId(), row.getTotal()));
        return result;
    }

    private Map<Long, Long> resolvePostLikeTotals(List<ForoPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = posts.stream().map(ForoPost::getId).toList();
        Map<Long, Long> result = new HashMap<>();
        foroPostReaccionRepository.countByPostIds(ids)
                .forEach(row -> result.put(row.getPostId(), row.getTotal()));
        return result;
    }

    private Map<Long, Long> resolveCommentLikeTotals(List<ForoComentario> comments) {
        if (comments == null || comments.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = comments.stream().map(ForoComentario::getId).toList();
        Map<Long, Long> result = new HashMap<>();
        foroComentarioReaccionRepository.countByComentarioIds(ids)
                .forEach(row -> result.put(row.getComentarioId(), row.getTotal()));
        return result;
    }

    private Set<Long> resolveLikedPostIds(List<ForoPost> posts, Long userId) {
        if (userId == null || posts == null || posts.isEmpty()) {
            return Set.of();
        }

        List<Long> ids = posts.stream().map(ForoPost::getId).toList();
        return new HashSet<>(foroPostReaccionRepository.findLikedPostIdsByUserAndPostIds(userId, ids));
    }

    private Set<Long> resolveLikedCommentIds(List<ForoComentario> comments, Long userId) {
        if (userId == null || comments == null || comments.isEmpty()) {
            return Set.of();
        }

        List<Long> ids = comments.stream().map(ForoComentario::getId).toList();
        return new HashSet<>(foroComentarioReaccionRepository.findLikedCommentIdsByUserAndCommentIds(userId, ids));
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

    private String resolveUniqueSlug(Long currentId, String requestedSlug, String titulo) {
        String base = slugify(requestedSlug == null || requestedSlug.isBlank() ? titulo : requestedSlug);

        List<String> usedSlugs = foroPostRepository.findUsedSlugsByBase(base, currentId);
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
            return "post";
        }

        return slug.length() > 240 ? slug.substring(0, 240) : slug;
    }
}
