package com.agencia.repository;

import com.agencia.domain.ForoComentarioReaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForoComentarioReaccionRepository extends JpaRepository<ForoComentarioReaccion, Long> {

    interface ForoComentarioLikeCountProjection {
        Long getComentarioId();

        Long getTotal();
    }

    Optional<ForoComentarioReaccion> findByComentarioIdAndUsuarioId(Long comentarioId, Long usuarioId);

    long countByComentarioId(Long comentarioId);

    @Query("""
            select r.comentario.id as comentarioId, count(r.id) as total
            from ForoComentarioReaccion r
            where r.comentario.id in :comentarioIds
            group by r.comentario.id
            """)
    List<ForoComentarioLikeCountProjection> countByComentarioIds(@Param("comentarioIds") List<Long> comentarioIds);

    @Query("""
            select r.comentario.id
            from ForoComentarioReaccion r
            where r.usuario.id = :usuarioId
              and r.comentario.id in :comentarioIds
            """)
    List<Long> findLikedCommentIdsByUserAndCommentIds(@Param("usuarioId") Long usuarioId, @Param("comentarioIds") List<Long> comentarioIds);
}
