package com.agencia.foro.service;

import com.agencia.auth.entity.Rol;
import com.agencia.auth.entity.Usuario;
import com.agencia.auth.repository.UsuarioRepository;
import com.agencia.foro.dto.ForoBloqueUpsertDTO;
import com.agencia.foro.dto.ForoComentarioCrearDTO;
import com.agencia.foro.dto.ForoPostUpsertDTO;
import com.agencia.foro.entity.ForoBloqueTipo;
import com.agencia.foro.entity.ForoPost;
import com.agencia.foro.entity.ForoPostBloque;
import com.agencia.foro.repository.ForoComentarioReaccionRepository;
import com.agencia.foro.repository.ForoComentarioRepository;
import com.agencia.foro.repository.ForoPostReaccionRepository;
import com.agencia.foro.repository.ForoPostRepository;
import com.agencia.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForoServiceTest {

    @Mock
    private ForoPostRepository foroPostRepository;

    @Mock
    private ForoComentarioRepository foroComentarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ForoPostReaccionRepository foroPostReaccionRepository;

    @Mock
    private ForoComentarioReaccionRepository foroComentarioReaccionRepository;

    @InjectMocks
    private ForoService foroService;

    @Test
    void crear_debePersistirPostConBloquesYSlugUnico() {
        Usuario admin = Usuario.builder().id(1L).usuario("admin").rol(Rol.ADMIN).build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(foroPostRepository.findUsedSlugsByBase("guia-cartagena", null)).thenReturn(List.of("guia-cartagena"));
        when(foroPostRepository.save(any(ForoPost.class))).thenReturn(ForoPost.builder().id(10L).build());

        ForoPostUpsertDTO dto = new ForoPostUpsertDTO(
                "Guia Cartagena",
                "guia-cartagena",
                "Todo lo que debes saber para viajar a Cartagena.",
                true,
                List.of(
                        new ForoBloqueUpsertDTO(ForoBloqueTipo.TEXTO, "Texto inicial del post", null, 0),
                        new ForoBloqueUpsertDTO(ForoBloqueTipo.IMAGEN, "foro/cartagena.webp", "Atardecer en la muralla", 1)
                )
        );

        Long id = foroService.crear(dto, 1L);

        assertEquals(10L, id);

        ArgumentCaptor<ForoPost> captor = ArgumentCaptor.forClass(ForoPost.class);
        verify(foroPostRepository).save(captor.capture());
        ForoPost guardado = captor.getValue();

        assertEquals("guia-cartagena-2", guardado.getSlug());
        assertEquals(2, guardado.getBloques().size());
        assertTrue(guardado.isPublicado());
        assertNotNull(guardado.getFechaPublicacion());
        assertNotNull(guardado.getUltimaModificacion());
        assertEquals(admin, guardado.getAutor());
    }

    @Test
    void crearComentario_debeFallarSiUsuarioNoEsCliente() {
        ForoPost post = ForoPost.builder().id(30L).slug("tips-europa").publicado(true).build();
        Usuario admin = Usuario.builder().id(5L).usuario("admin").rol(Rol.ADMIN).build();

        when(foroPostRepository.findDetalleBaseBySlug("tips-europa")).thenReturn(Optional.of(post));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(admin));

        assertThrows(
                ReglaNegocioException.class,
                () -> foroService.crearComentario("tips-europa", new ForoComentarioCrearDTO("Gran artículo"), 5L)
        );

        verify(foroComentarioRepository, never()).save(any());
    }

    @Test
    void listarPublicos_debeMapearImagenPortadaYTotalComentarios() {
        ReflectionTestUtils.setField(foroService, "storagePublicBaseUrl", "http://localhost/images");

        ForoPost post = ForoPost.builder()
                .id(44L)
                .titulo("Ruta por el eje cafetero")
                .slug("ruta-eje-cafetero")
                .resumen("Post de prueba")
                .publicado(true)
                .fechaPublicacion(LocalDateTime.now().minusDays(1))
                .ultimaModificacion(LocalDateTime.now())
                .bloques(List.of(
                        ForoPostBloque.builder().tipo(ForoBloqueTipo.TEXTO).contenido("Intro").orden(0).build(),
                        ForoPostBloque.builder().tipo(ForoBloqueTipo.IMAGEN).contenido("foro/eje.webp").orden(1).build()
                ))
                .build();

        when(foroPostRepository.findPublicByQuery(isNull(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 9), 1));
        when(foroComentarioRepository.countVisibleByPostIds(eq(List.of(44L))))
                .thenReturn(List.of(countProjection(44L, 3L)));
        when(foroPostReaccionRepository.countByPostIds(eq(List.of(44L))))
            .thenReturn(List.of());

        var result = foroService.listarPublicos(null, 0, 9, null);
        var posts = result.items();

        assertEquals(1, posts.size());
        assertEquals("http://localhost/images/foro/eje.webp", posts.getFirst().imagenPortada());
        assertEquals(3L, posts.getFirst().totalComentarios());
    }

    @Test
    void actualizar_debeFallarSiNoHayBloquesTexto() {
        Usuario admin = Usuario.builder().id(9L).usuario("admin").rol(Rol.ADMIN).build();
        ForoPost existing = ForoPost.builder().id(77L).autor(admin).slug("base").build();
        when(foroPostRepository.findDetalleBaseById(77L)).thenReturn(Optional.of(existing));

        ForoPostUpsertDTO dto = new ForoPostUpsertDTO(
                "Solo imagenes",
                "solo-imagenes",
                "Resumen",
                true,
                List.of(new ForoBloqueUpsertDTO(ForoBloqueTipo.IMAGEN, "foro/solo.webp", null, 0))
        );

        assertThrows(ReglaNegocioException.class, () -> foroService.actualizar(77L, dto));
        verify(foroPostRepository, never()).save(any());
    }

    private ForoComentarioRepository.ForoComentarioCountProjection countProjection(Long postId, Long total) {
        return new ForoComentarioRepository.ForoComentarioCountProjection() {
            @Override
            public Long getPostId() {
                return postId;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }
}
