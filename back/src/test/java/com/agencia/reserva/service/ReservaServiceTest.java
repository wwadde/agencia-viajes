package com.agencia.reserva.service;

import com.agencia.auth.entity.Usuario;
import com.agencia.auth.repository.UsuarioRepository;
import com.agencia.paquete.entity.Paquete;
import com.agencia.paquete.entity.Salida;
import com.agencia.paquete.repository.SalidaRepository;
import com.agencia.reserva.dto.ReservaCrearDTO;
import com.agencia.reserva.dto.ReservaEstadoUpdateDTO;
import com.agencia.reserva.entity.Reserva;
import com.agencia.reserva.entity.ReservaEstado;
import com.agencia.reserva.repository.ReservaRepository;
import com.agencia.shared.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalidaRepository salidaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void crear_debeDescontarCuposYPersistirReserva() {
        Usuario usuario = Usuario.builder().id(2L).usuario("cliente01").build();
        Paquete paquete = Paquete.builder().id(4L).nombre("Caribe").slug("caribe").build();
        Salida salida = Salida.builder()
                .id(7L)
                .paquete(paquete)
                .fechaSalida(LocalDate.now().plusDays(10))
                .precio(new BigDecimal("350.50"))
                .cuposDisponibles(6)
                .build();

        when(usuarioRepository.getReferenceById(2L)).thenReturn(usuario);
        when(salidaRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(salida));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(Reserva.builder().id(22L).build());

        Long id = reservaService.crear(new ReservaCrearDTO(7L, 3, "Sin gluten"), 2L);

        assertEquals(22L, id);
        assertEquals(3, salida.getCuposDisponibles());

        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(captor.capture());
        Reserva guardada = captor.getValue();

        assertEquals(ReservaEstado.PENDIENTE, guardada.getEstado());
        assertEquals(new BigDecimal("350.50"), guardada.getPrecioUnitario());
        assertEquals(new BigDecimal("1051.50"), guardada.getPrecioTotal());
    }

    @Test
    void crear_debeFallarCuandoNoHayCuposSuficientes() {
        Usuario usuario = Usuario.builder().id(5L).usuario("cliente02").build();
        Salida salida = Salida.builder()
                .id(9L)
                .fechaSalida(LocalDate.now().plusDays(20))
                .precio(BigDecimal.valueOf(1000))
                .cuposDisponibles(1)
                .build();

        when(usuarioRepository.getReferenceById(5L)).thenReturn(usuario);
        when(salidaRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(salida));

        assertThrows(ReglaNegocioException.class, () -> reservaService.crear(new ReservaCrearDTO(9L, 2, null), 5L));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void cancelarMia_debeLiberarCuposYMarcarCancelada() {
        Usuario usuario = Usuario.builder().id(3L).usuario("cliente03").build();
        Salida salida = Salida.builder()
                .id(12L)
                .precio(BigDecimal.valueOf(700))
                .cuposDisponibles(4)
                .build();
        Reserva reserva = Reserva.builder()
                .id(99L)
                .usuario(usuario)
                .salida(salida)
                .fechaReserva(LocalDateTime.now().minusDays(1))
                .cantidadPersonas(2)
                .precioUnitario(BigDecimal.valueOf(700))
                .precioTotal(BigDecimal.valueOf(1400))
                .estado(ReservaEstado.PENDIENTE)
                .build();

        when(reservaRepository.findByIdAndUsuarioId(99L, 3L)).thenReturn(Optional.of(reserva));
        when(salidaRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(salida));

        reservaService.cancelarMia(99L, 3L);

        assertEquals(ReservaEstado.CANCELADA, reserva.getEstado());
        assertEquals(6, salida.getCuposDisponibles());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void actualizarEstadoAdmin_debeConfirmarReservaPendiente() {
        Usuario usuario = Usuario.builder().id(8L).usuario("cliente04").build();
        Salida salida = Salida.builder().id(30L).cuposDisponibles(10).build();
        Reserva reserva = Reserva.builder()
                .id(50L)
                .usuario(usuario)
                .salida(salida)
                .estado(ReservaEstado.PENDIENTE)
                .cantidadPersonas(1)
                .precioUnitario(BigDecimal.valueOf(100))
                .precioTotal(BigDecimal.valueOf(100))
                .build();

        when(reservaRepository.findById(50L)).thenReturn(Optional.of(reserva));

        reservaService.actualizarEstadoAdmin(50L, new ReservaEstadoUpdateDTO(ReservaEstado.CONFIRMADA));

        assertEquals(ReservaEstado.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }
}
