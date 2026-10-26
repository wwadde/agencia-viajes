package com.agencia.contenido.service;

import com.agencia.contenido.dto.ContenidoUpsertDTO;
import com.agencia.contenido.entity.Contenido;
import com.agencia.contenido.repository.ContenidoRepository;
import com.agencia.shared.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContenidoServiceTest {

    @Mock
    private ContenidoRepository contenidoRepository;

    @InjectMocks
    private ContenidoService contenidoService;

    @Test
    void crear_debePersistirClaveNormalizada() {
        when(contenidoRepository.save(any(Contenido.class))).thenReturn(Contenido.builder().id(8L).build());

        Long id = contenidoService.crear(new ContenidoUpsertDTO("  home.hero.title  ", "Viaja con nosotros"));

        assertEquals(8L, id);
        ArgumentCaptor<Contenido> captor = ArgumentCaptor.forClass(Contenido.class);
        verify(contenidoRepository).save(captor.capture());
        assertEquals("home.hero.title", captor.getValue().getClave());
    }

    @Test
    void listar_debeOrdenarPorClave() {
        when(contenidoRepository.findAll()).thenReturn(List.of(
                Contenido.builder().id(2L).clave("home.z").valor("z").build(),
                Contenido.builder().id(1L).clave("home.a").valor("a").build()
        ));

        var contenidos = contenidoService.listar();

        assertEquals(2, contenidos.size());
        assertEquals("home.a", contenidos.getFirst().clave());
        assertEquals("home.z", contenidos.get(1).clave());
    }

    @Test
    void obtenerPorClave_debeLanzar404SiNoExiste() {
        when(contenidoRepository.findByClave("home.missing")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> contenidoService.obtenerPorClave("home.missing"));
    }
}

