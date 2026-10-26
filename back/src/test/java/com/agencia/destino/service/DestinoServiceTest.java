package com.agencia.destino.service;

import com.agencia.destino.dto.DestinoDTO;
import com.agencia.destino.entity.Destino;
import com.agencia.destino.mapper.DestinoMapper;
import com.agencia.destino.repository.DestinoRepository;
import com.agencia.shared.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
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

    private final DestinoMapper destinoMapper = Mappers.getMapper(DestinoMapper.class);

    private DestinoService destinoService;

    @BeforeEach
    void setUp() {
        destinoService = new DestinoService(destinoRepository, destinoMapper);
    }

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

