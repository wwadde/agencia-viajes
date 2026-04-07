package com.agencia.service;

import com.agencia.domain.Destino;
import com.agencia.domain.ImagenPaquete;
import com.agencia.domain.Paquete;
import com.agencia.domain.dto.ImagenPaqueteUpsertDTO;
import com.agencia.domain.dto.PaqueteUpsertDTO;
import com.agencia.domain.dto.SalidaUpsertDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.DestinoRepository;
import com.agencia.repository.PaqueteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaqueteServiceTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private DestinoRepository destinoRepository;

    @InjectMocks
    private PaqueteService paqueteService;

    @Test
    void crear_debePersistirPaqueteConDestinoYSalidas() {
        Destino destino = Destino.builder().id(7L).nombre("Cartagena").build();
        when(destinoRepository.findById(7L)).thenReturn(Optional.of(destino));
        when(paqueteRepository.findUsedSlugsByBase("caribe-premium", null)).thenReturn(List.of());

        Paquete persisted = Paquete.builder().id(11L).build();
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(persisted);

        PaqueteUpsertDTO dto = new PaqueteUpsertDTO(
                "Caribe Premium",
                "caribe-premium",
                "Plan de 5 dias",
                "Incluye vuelos y hotel",
                5,
                true,
                true,
                7L,
                List.of(
                        new ImagenPaqueteUpsertDTO("paquetes/a.jpg", 0, true),
                        new ImagenPaqueteUpsertDTO("paquetes/b.jpg", 1, false)
                ),
                List.of(new SalidaUpsertDTO(LocalDate.now().plusDays(15), BigDecimal.valueOf(2499.99), 12))
        );

        Long id = paqueteService.crear(dto);

        assertEquals(11L, id);

        ArgumentCaptor<Paquete> captor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(captor.capture());

        Paquete paqueteGuardado = captor.getValue();
        assertEquals("Caribe Premium", paqueteGuardado.getNombre());
        assertEquals("caribe-premium", paqueteGuardado.getSlug());
        assertEquals(destino, paqueteGuardado.getDestino());
        assertEquals(2, paqueteGuardado.getImagenes().size());
        assertEquals("paquetes/b.jpg", paqueteGuardado.getImagenes().get(1).getUrl());
        assertEquals(0, paqueteGuardado.getImagenes().get(0).getOrden());
        assertEquals(1, paqueteGuardado.getImagenes().stream().filter(ImagenPaquete::isPrincipal).count());
        assertNotNull(paqueteGuardado.getUltimaModificacion());
        assertTrue(paqueteGuardado.getSalidas().isEmpty());
    }

    @Test
    void crear_debeResolverSlugConUnaSolaConsulta() {
        Destino destino = Destino.builder().id(9L).nombre("Cusco").build();
        when(destinoRepository.findById(9L)).thenReturn(Optional.of(destino));
        when(paqueteRepository.findUsedSlugsByBase("machu-picchu", null))
                .thenReturn(List.of("machu-picchu", "machu-picchu-2"));
        when(paqueteRepository.save(any(Paquete.class))).thenReturn(Paquete.builder().id(31L).build());

        PaqueteUpsertDTO dto = new PaqueteUpsertDTO(
                "Machu Picchu",
                "machu-picchu",
                "Descripcion",
                "Detalle",
                6,
                true,
                true,
                9L,
                List.of(new ImagenPaqueteUpsertDTO("paquetes/a.jpg", 0, true)),
                List.of(new SalidaUpsertDTO(LocalDate.now().plusDays(10), BigDecimal.valueOf(1800), 20))
        );

        paqueteService.crear(dto);

        ArgumentCaptor<Paquete> captor = ArgumentCaptor.forClass(Paquete.class);
        verify(paqueteRepository).save(captor.capture());
        assertEquals("machu-picchu-3", captor.getValue().getSlug());
        verify(paqueteRepository).findUsedSlugsByBase(eq("machu-picchu"), eq(null));
        verify(paqueteRepository, never()).existsBySlug(any());
        verify(paqueteRepository, never()).existsBySlugAndIdNot(any(), any());
    }

        @Test
        void crear_debeNormalizarTildesYEnieEnSlug() {
                Destino destino = Destino.builder().id(12L).nombre("Chile").build();
                when(destinoRepository.findById(12L)).thenReturn(Optional.of(destino));
                when(paqueteRepository.findUsedSlugsByBase("aventura-en-canon-del-nuble", null)).thenReturn(List.of());
                when(paqueteRepository.save(any(Paquete.class))).thenReturn(Paquete.builder().id(55L).build());

                PaqueteUpsertDTO dto = new PaqueteUpsertDTO(
                                "Aventura en Cañón del Ñuble",
                                "",
                                "Descripcion",
                                "Detalle",
                                4,
                                true,
                                false,
                                12L,
                                List.of(new ImagenPaqueteUpsertDTO("paquetes/nuble.jpg", 0, true)),
                                List.of()
                );

                paqueteService.crear(dto);

                ArgumentCaptor<Paquete> captor = ArgumentCaptor.forClass(Paquete.class);
                verify(paqueteRepository).save(captor.capture());
                assertEquals("aventura-en-canon-del-nuble", captor.getValue().getSlug());
        }

    @Test
    void actualizar_debeConservarImagenesSiNoSeEnviaCampoImagenes() {
        Paquete paquete = Paquete.builder()
                .id(40L)
                .imagenes(new java.util.ArrayList<>(List.of(
                        ImagenPaquete.builder().url("paquetes/existente.jpg").orden(0).principal(true).build()
                )))
                .salidas(new java.util.ArrayList<>())
                .build();
        Destino destino = Destino.builder().id(3L).nombre("Santa Marta").build();
        when(paqueteRepository.findDetalleBaseById(40L)).thenReturn(Optional.of(paquete));
        when(destinoRepository.findById(3L)).thenReturn(Optional.of(destino));
        when(paqueteRepository.findUsedSlugsByBase("plan-caribe", 40L)).thenReturn(List.of());

        PaqueteUpsertDTO dto = new PaqueteUpsertDTO(
                "Plan Caribe",
                "plan-caribe",
                "Descripcion",
                "Detalle",
                4,
                true,
                false,
                3L,
                null,
                List.of()
        );

        paqueteService.actualizar(40L, dto);
        assertEquals(1, paquete.getImagenes().size());
        assertEquals("paquetes/existente.jpg", paquete.getImagenes().getFirst().getUrl());
        assertNotNull(paquete.getUltimaModificacion());
    }

    @Test
    void actualizar_conImagenes_debeReemplazarColeccionYMantenerUnaPrincipal() {
        Paquete paquete = Paquete.builder()
                .id(41L)
                .ultimaModificacion(LocalDateTime.now().minusDays(3))
                .imagenes(new java.util.ArrayList<>(List.of(
                        ImagenPaquete.builder().url("paquetes/existente.jpg").orden(0).principal(true).build()
                )))
                .salidas(new java.util.ArrayList<>())
                .build();
        LocalDateTime fechaAnterior = paquete.getUltimaModificacion();
        Destino destino = Destino.builder().id(5L).nombre("Lima").build();
        when(paqueteRepository.findDetalleBaseById(41L)).thenReturn(Optional.of(paquete));
        when(destinoRepository.findById(5L)).thenReturn(Optional.of(destino));
        when(paqueteRepository.findUsedSlugsByBase("lima-escape", 41L)).thenReturn(List.of());

        PaqueteUpsertDTO dto = new PaqueteUpsertDTO(
                "Lima Escape",
                "lima-escape",
                "Descripcion",
                "Detalle",
                3,
                true,
                false,
                5L,
                List.of(
                        new ImagenPaqueteUpsertDTO("paquetes/nueva-a.jpg", 0, false),
                        new ImagenPaqueteUpsertDTO("paquetes/nueva-b.jpg", 1, true)
                ),
                List.of()
        );

        paqueteService.actualizar(41L, dto);

        verify(paqueteRepository).saveAndFlush(paquete);
        assertEquals(2, paquete.getImagenes().size());
        assertEquals(1, paquete.getImagenes().stream().filter(ImagenPaquete::isPrincipal).count());
        assertEquals("paquetes/nueva-b.jpg", paquete.getImagenes().get(1).getUrl());
        assertNotNull(paquete.getUltimaModificacion());
        assertTrue(paquete.getUltimaModificacion().isAfter(fechaAnterior));
    }

    @Test
    void obtenerPublico_debeLanzar404SiNoEstaDisponible() {
        Paquete oculto = Paquete.builder().id(99L).disponible(false).build();
        when(paqueteRepository.findDetalleBaseById(99L)).thenReturn(Optional.of(oculto));

        assertThrows(RecursoNoEncontradoException.class, () -> paqueteService.obtenerPublico("99"));
    }

    @Test
    void obtenerPublico_debePermitirBusquedaPorSlug() {
        Paquete paquete = Paquete.builder()
                .id(77L)
                .slug("cartagena-premium")
                .nombre("Cartagena Premium")
                .descripcion("Desc")
                .detalle("Det")
                .duracionDias(5)
                .disponible(true)
                .destacado(true)
                .imagenes(new java.util.ArrayList<>())
                .salidas(new java.util.ArrayList<>())
                .build();

        when(paqueteRepository.findDetalleBaseBySlug("cartagena-premium")).thenReturn(Optional.of(paquete));

        var detalle = paqueteService.obtenerPublico("cartagena-premium");
        assertEquals(77L, detalle.id());
        assertEquals("cartagena-premium", detalle.slug());
    }

    @Test
    void obtenerPublico_debeFallarSiNoSeEnviaIdNiSlug() {
        assertThrows(ReglaNegocioException.class, () -> paqueteService.obtenerPublico("   "));
    }
}
