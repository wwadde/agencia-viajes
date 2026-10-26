package com.agencia.foro.repository;

import com.agencia.foro.entity.ForoComentario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForoComentarioRepository extends JpaRepository<ForoComentario, Long> {

    interface ForoComentarioCountProjection {
        Long getPostId();

        Long getTotal();
    }

    @EntityGraph(attributePaths = {"usuario"})
        List<ForoComentario> findByPostIdOrderByFechaCreacionAscIdAsc(Long postId);

        @EntityGraph(attributePaths = {"usuario"})
        List<ForoComentario> findByPostIdAndOcultoFalseOrderByFechaCreacionAscIdAsc(Long postId);

        Optional<ForoComentario> findByIdAndPostId(Long id, Long postId);

    @Query("""
            select c.post.id as postId, count(c.id) as total
            from ForoComentario c
            where c.oculto = false
              and c.post.id in :postIds
            group by c.post.id
            """)
        List<ForoComentarioCountProjection> countVisibleByPostIds(@Param("postIds") List<Long> postIds);

        @Query("""
            select c.post.id as postId, count(c.id) as total
            from ForoComentario c
            where c.post.id in :postIds
            group by c.post.id
            """)
        List<ForoComentarioCountProjection> countAllByPostIds(@Param("postIds") List<Long> postIds);
}
