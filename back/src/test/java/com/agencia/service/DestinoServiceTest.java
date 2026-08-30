package com.agencia.service;

import com.agencia.domain.Destino;
import com.agencia.domain.dto.DestinoDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.repository.DestinoRepository;
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
class DestinoServiceTest {

    @Mock
    private DestinoRepository destinoRepository;

    @InjectMocks
    private DestinoService destinoService;

    @Test
    void crear_debePersistirCamposNormalizados() {
        when(destinoRepository.save(any(Destino.class))).thenReturn(Destino.builder().id(5L).build());

        Long id = destinoService.crear(new DestinoDTO("  Cartagena ", " Colombia ", " Cartagena ", null, null));

        assertEquals(5L, id);
        ArgumentCaptor<Destino> captor = ArgumentCaptor.forClass(Destino.class);
        verify(destinoRepository).save(captor.capture());
        assertEquals("Cartagena", captor.getValue().getNombre());
        assertEquals("Colombia", captor.getValue().getPais());
        assertEquals("Cartagena", captor.getValue().getCiudad());
    }

    @Test
    void listar_debeOrdenarPorNombrePaisCiudad() {
        when(destinoRepository.findAll()).thenReturn(List.of(
                Destino.builder().id(2L).nombre("San Andres").pais("Colombia").ciudad("San Andres").build(),
                Destino.builder().id(1L).nombre("Cartagena").pais("Colombia").ciudad("Cartagena").build()
        ));

        var destinos = destinoService.listar();

        assertEquals(2, destinos.size());
        assertEquals("Cartagena", destinos.getFirst().nombre());
        assertEquals("San Andres", destinos.get(1).nombre());
    }

    @Test
    void obtener_debeLanzar404CuandoNoExiste() {
        when(destinoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> destinoService.obtener(99L));
    }
}

