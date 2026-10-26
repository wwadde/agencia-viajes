package com.agencia.service;

import com.agencia.config.mapper.ReservaMapper;
import com.agencia.domain.Paquete;
import com.agencia.domain.Reserva;
import com.agencia.domain.ReservaEstado;
import com.agencia.domain.Salida;
import com.agencia.domain.Usuario;
import com.agencia.domain.dto.ReservaCrearDTO;
import com.agencia.domain.dto.ReservaDTO;
import com.agencia.domain.dto.ReservaEstadoUpdateDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.exception.ReglaNegocioException;
import com.agencia.repository.ReservaRepository;
import com.agencia.repository.SalidaRepository;
import com.agencia.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalidaRepository salidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaMapper destinoMapper;

    public List<ReservaDTO> listarMisReservas(Long usuarioId) {
        return reservaRepository.findByUsuarioIdOrderByFechaReservaDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ReservaDTO obtenerMia(Long id, Long usuarioId) {
        return toDTO(getMineOrThrow(id, usuarioId));
    }

    @Transactional
    public Long crear(ReservaCrearDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Salida salida = getSalidaForUpdateOrThrow(dto.salidaId());

        if (salida.getFechaSalida() != null && salida.getFechaSalida().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No puede reservar una salida que ya ocurrió");
        }

        int cantidad = dto.cantidadPersonas();
        int cuposActuales = salida.getCuposDisponibles() == null ? 0 : salida.getCuposDisponibles();
        if (cuposActuales < cantidad) {
            throw new ReglaNegocioException("No hay cupos suficientes para completar la reserva");
        }

        salida.setCuposDisponibles(cuposActuales - cantidad);
        salidaRepository.save(salida);

        BigDecimal precioUnitario = salida.getPrecio();
        BigDecimal precioTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

        Reserva reserva = Reserva.builder()
                .usuario(usuario)
                .salida(salida)
                .fechaReserva(LocalDateTime.now())
                .cantidadPersonas(cantidad)
                .precioUnitario(precioUnitario)
                .precioTotal(precioTotal)
                .estado(ReservaEstado.PENDIENTE)
                .observaciones(dto.observaciones() == null ? null : dto.observaciones().trim())
                .build();

        return reservaRepository.save(reserva).getId();
    }

    @Transactional
    public void cancelarMia(Long id, Long usuarioId) {
        Reserva reserva = getMineOrThrow(id, usuarioId);
        cancelarReserva(reserva);
    }

    public List<ReservaDTO> listarAdmin(ReservaEstado estado) {
        List<Reserva> reservas = estado == null
                ? reservaRepository.findAllByOrderByFechaReservaDesc()
                : reservaRepository.findByEstadoOrderByFechaReservaDesc(estado);
        return reservas.stream().map(this::toDTO).toList();
    }

    public ReservaDTO obtenerAdmin(Long id) {
        return toDTO(getByIdOrThrow(id));
    }

    @Transactional
    public void actualizarEstadoAdmin(Long id, ReservaEstadoUpdateDTO dto) {
        Reserva reserva = getByIdOrThrow(id);
        ReservaEstado nuevoEstado = dto.estado();

        if (reserva.getEstado() == nuevoEstado) {
            return;
        }

        if (reserva.getEstado() == ReservaEstado.CANCELADA) {
            throw new ReglaNegocioException("Una reserva cancelada no puede cambiar de estado");
        }

        if (nuevoEstado == ReservaEstado.CANCELADA) {
            cancelarReserva(reserva);
            return;
        }

        if (nuevoEstado != ReservaEstado.CONFIRMADA) {
            throw new ReglaNegocioException("Estado de reserva no soportado");
        }

        reserva.setEstado(ReservaEstado.CONFIRMADA);
        reservaRepository.save(reserva);
    }

    private void cancelarReserva(Reserva reserva) {
        if (reserva.getEstado() == ReservaEstado.CANCELADA) {
            return;
        }

        Salida salida = getSalidaForUpdateOrThrow(reserva.getSalida().getId());
        int cuposActuales = salida.getCuposDisponibles() == null ? 0 : salida.getCuposDisponibles();
        salida.setCuposDisponibles(cuposActuales + reserva.getCantidadPersonas());

        reserva.setSalida(salida);
        reserva.setEstado(ReservaEstado.CANCELADA);

        salidaRepository.save(salida);
        reservaRepository.save(reserva);
    }

    private Reserva getMineOrThrow(Long id, Long usuarioId) {
        return reservaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + id));
    }

    private Reserva getByIdOrThrow(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + id));
    }

    private Salida getSalidaForUpdateOrThrow(Long salidaId) {
        return salidaRepository.findByIdForUpdate(salidaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada: " + salidaId));
    }

    public List<ReservaDTO> filtrarMax5Reservas(ReservaEstado estado, Long usuarioId) {
        Pageable limit = PageRequest.of(0, 5);
        return reservaRepository.filtrarReservasPorUsuario(estado, usuarioId, limit)
                .stream()
                .map(destinoMapper::toDTO)
                .toList();
    }

    private ReservaDTO toDTO(Reserva reserva) {
        Salida salida = reserva.getSalida();
        Paquete paquete = salida != null ? salida.getPaquete() : null;
        Usuario cliente = reserva.getUsuario();

        return new ReservaDTO(
                reserva.getId(),
                reserva.getFechaReserva(),
                reserva.getEstado(),
                reserva.getCantidadPersonas(),
                reserva.getPrecioUnitario(),
                reserva.getPrecioTotal(),
                reserva.getObservaciones(),
                salida != null ? salida.getId() : null,
                salida != null ? salida.getFechaSalida() : null,
                paquete != null ? paquete.getId() : null,
                paquete != null ? paquete.getNombre() : null,
                paquete != null ? paquete.getSlug() : null,
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getUsername() : null
        );
    }
}

