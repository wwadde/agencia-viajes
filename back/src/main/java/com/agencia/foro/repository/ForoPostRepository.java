package com.agencia.foro.repository;

import com.agencia.foro.entity.ForoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ForoPostRepository extends JpaRepository<ForoPost, Long> {

    interface ForoPostSitemapProjection {
        String getSlug();

        LocalDateTime getUltimaModificacion();
    }

    @Query("""
            select p.slug
            from ForoPost p
            where (p.slug = :baseSlug or p.slug like concat(:baseSlug, '-%'))
              and (:excludeId is null or p.id <> :excludeId)
            """)
    List<String> findUsedSlugsByBase(@Param("baseSlug") String baseSlug, @Param("excludeId") Long excludeId);

    @EntityGraph(attributePaths = {"bloques", "autor"})
    List<ForoPost> findByPublicadoTrueOrderByFechaPublicacionDescUltimaModificacionDesc();

    @Query("""
            select p
            from ForoPost p
            where p.publicado = true
              and (
                :query is null
                or :query = ''
                or lower(p.titulo) like concat('%', lower(:query), '%')
                or lower(p.resumen) like concat('%', lower(:query), '%')
              )
            """)
    Page<ForoPost> findPublicByQuery(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"bloques", "autor"})
    List<ForoPost> findAllByOrderByFechaPublicacionDescUltimaModificacionDesc();

    @EntityGraph(attributePaths = {"bloques", "autor"})
    @Query("""
            select p
            from ForoPost p
            where p.id = :id
            """)
    Optional<ForoPost> findDetalleBaseById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"bloques", "autor"})
    @Query("""
            select p
            from ForoPost p
            where p.slug = :slug
            """)
    Optional<ForoPost> findDetalleBaseBySlug(@Param("slug") String slug);

    @Query("""
            select p.slug as slug, p.ultimaModificacion as ultimaModificacion
            from ForoPost p
            where p.publicado = true
              and p.slug is not null
              and trim(p.slug) <> ''
            order by p.fechaPublicacion desc, p.ultimaModificacion desc
            """)
    List<ForoPostSitemapProjection> findPublicSlugsForSitemap();
}
