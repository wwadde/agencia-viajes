package com.agencia.repository;

import com.agencia.domain.Paquete;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository extends JpaRepository<Paquete, Long>, JpaSpecificationExecutor<Paquete> {

    Optional<Paquete> findByNombre(String nombre);

    interface PaqueteSitemapProjection {
        String getSlug();

        java.time.LocalDateTime getUltimaModificacion();
    }

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("""
            select p.slug
            from Paquete p
            where (p.slug = :baseSlug or p.slug like concat(:baseSlug, '-%'))
              and (:excludeId is null or p.id <> :excludeId)
            """)
    List<String> findUsedSlugsByBase(@Param("baseSlug") String baseSlug, @Param("excludeId") Long excludeId);

    @EntityGraph(attributePaths = {"destino", "imagenes"})
    List<Paquete> findByDisponibleTrueOrderByDestacadoDescNombreAsc();

    @Query("""
            select p.slug as slug, p.ultimaModificacion as ultimaModificacion
            from Paquete p
            where p.disponible = true
              and p.slug is not null
              and trim(p.slug) <> ''
            order by p.destacado desc, p.nombre asc
            """)
    List<PaqueteSitemapProjection> findPublicSlugsForSitemap();

    @EntityGraph(attributePaths = {"destino", "imagenes"})
    List<Paquete> findAllByOrderByDestacadoDescNombreAsc();

    @EntityGraph(attributePaths = {"destino", "imagenes"})
    @Query("""
            select p
            from Paquete p
            where p.id = :id
            """)
    Optional<Paquete> findDetalleBaseById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"destino", "imagenes"})
    @Query("""
            select p
            from Paquete p
            where p.slug = :slug
            """)
    Optional<Paquete> findDetalleBaseBySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"destino"})
    @Query("""
        SELECT p FROM Paquete p WHERE p.id = :id
        """)
    Optional<Paquete> encontrarPaqueteConDestino(Long id);
}

