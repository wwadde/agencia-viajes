package com.agencia.agenda.service;

import com.agencia.agenda.dto.AgendaSalidaUpsertDTO;
import com.agencia.paquete.entity.Paquete;
import com.agencia.paquete.entity.Salida;
import com.agencia.paquete.repository.PaqueteRepository;
import com.agencia.paquete.repository.SalidaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private SalidaRepository salidaRepository;

    @Mock
    private PaqueteRepository paqueteRepository;

    @InjectMocks
    private AgendaService agendaService;

    @Test
    void listarProximas_debeMapearDatosDePaquete() {
        Paquete paquete = Paquete.builder().id(10L).nombre("Caribe Premium").slug("caribe-premium").build();
        Salida salida = Salida.builder()
                .id(3L)
                .fechaSalida(LocalDate.now().plusDays(5))
                .precio(BigDecimal.valueOf(1200))
                .cuposDisponibles(8)
                .paquete(paquete)
                .build();

        when(salidaRepository.findByFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(any(LocalDate.class)))
                .thenReturn(List.of(salida));

        var result = agendaService.listarProximas(null);

        assertEquals(1, result.size());
        assertEquals("Caribe Premium", result.getFirst().paqueteNombre());
        assertEquals("caribe-premium", result.getFirst().paqueteSlug());
    }

    @Test
    void listarProximas_debeFiltrarPorPaqueteCuandoSeRecibeParametro() {
        when(salidaRepository.findByPaqueteIdAndFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(any(Long.class), any(LocalDate.class)))
                .thenReturn(List.of());

        agendaService.listarProximas(7L);

        verify(salidaRepository).findByPaqueteIdAndFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(any(Long.class), any(LocalDate.class));
    }

    @Test
    void crear_debePersistirSalidaConPaquete() {
        Paquete paquete = Paquete.builder().id(4L).nombre("Andes").slug("andes").build();
        when(paqueteRepository.findById(4L)).thenReturn(Optional.of(paquete));
        when(salidaRepository.save(any(Salida.class))).thenReturn(Salida.builder().id(55L).build());

        Long id = agendaService.crear(new AgendaSalidaUpsertDTO(
                LocalDate.now().plusDays(12),
                BigDecimal.valueOf(1890),
                16,
                4L
        ));

        assertEquals(55L, id);
        verify(salidaRepository).save(any(Salida.class));
    }
}

