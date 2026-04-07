package com.agencia.repository;

import com.agencia.domain.ForoPostReaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForoPostReaccionRepository extends JpaRepository<ForoPostReaccion, Long> {

    interface ForoPostLikeCountProjection {
        Long getPostId();

        Long getTotal();
    }

    Optional<ForoPostReaccion> findByPostIdAndUsuarioId(Long postId, Long usuarioId);

    long countByPostId(Long postId);

    @Query("""
            select r.post.id as postId, count(r.id) as total
            from ForoPostReaccion r
            where r.post.id in :postIds
            group by r.post.id
            """)
    List<ForoPostLikeCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
            select r.post.id
            from ForoPostReaccion r
            where r.usuario.id = :usuarioId
              and r.post.id in :postIds
            """)
    List<Long> findLikedPostIdsByUserAndPostIds(@Param("usuarioId") Long usuarioId, @Param("postIds") List<Long> postIds);
}
