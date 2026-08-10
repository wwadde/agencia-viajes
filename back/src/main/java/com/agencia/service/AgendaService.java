package com.agencia.service;

import com.agencia.domain.Paquete;
import com.agencia.domain.Salida;
import com.agencia.domain.dto.AgendaSalidaDTO;
import com.agencia.domain.dto.AgendaSalidaUpsertDTO;
import com.agencia.domain.dto.DisponibilidadDTO;
import com.agencia.domain.dto.SalidaDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.repository.PaqueteRepository;
import com.agencia.repository.SalidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private final SalidaRepository salidaRepository;
    private final PaqueteRepository paqueteRepository;

    public List<AgendaSalidaDTO> listarProximas(Long paqueteId) {
        LocalDate hoy = LocalDate.now();
        List<Salida> salidas = paqueteId == null
                ? salidaRepository.findByFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(hoy)
                : salidaRepository.findByPaqueteIdAndFechaSalidaGreaterThanEqualOrderByFechaSalidaAsc(paqueteId, hoy);

        return salidas.stream().map(this::toDTO).toList();
    }

    public List<AgendaSalidaDTO> listarAdmin(Long paqueteId) {
        List<Salida> salidas = paqueteId == null
                ? salidaRepository.findAllByOrderByFechaSalidaAsc()
                : salidaRepository.findByPaqueteIdOrderByFechaSalidaAsc(paqueteId);
        return salidas.stream().map(this::toDTO).toList();
    }

    public AgendaSalidaDTO obtenerAdmin(Long id) {
        return toDTO(getByIdOrThrow(id));
    }

    @Transactional
    public Long crear(AgendaSalidaUpsertDTO dto) {
        Paquete paquete = getPaqueteOrThrow(dto.paqueteId());

        Salida salida = Salida.builder().build();
        apply(salida, dto, paquete);
        return salidaRepository.save(salida).getId();
    }

    @Transactional
    public void actualizar(Long id, AgendaSalidaUpsertDTO dto) {
        Salida salida = getByIdOrThrow(id);
        Paquete paquete = getPaqueteOrThrow(dto.paqueteId());
        apply(salida, dto, paquete);
        salidaRepository.save(salida);
    }

    public void eliminar(Long id) {
        salidaRepository.delete(getByIdOrThrow(id));
    }

    private Salida getByIdOrThrow(Long id) {
        return salidaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Salida no encontrada: " + id));
    }

    private Paquete getPaqueteOrThrow(Long paqueteId) {
        return paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete no encontrado: " + paqueteId));
    }

    private void apply(Salida salida, AgendaSalidaUpsertDTO dto, Paquete paquete) {
        salida.setFechaSalida(dto.fechaSalida());
        salida.setPrecio(dto.precio());
        salida.setCuposDisponibles(dto.cuposDisponibles());
        salida.setPaquete(paquete);
    }

    private AgendaSalidaDTO toDTO(Salida salida) {
        Paquete paquete = salida.getPaquete();
        return new AgendaSalidaDTO(
                salida.getId(),
                salida.getFechaSalida(),
                salida.getPrecio(),
                salida.getCuposDisponibles(),
                paquete != null ? paquete.getId() : null,
                paquete != null ? paquete.getNombre() : null,
                paquete != null ? paquete.getSlug() : null
        );
    }

    public List<SalidaDTO> obtenerProximasSalidas(
            Long paqueteId,
            LocalDate desde,
            Integer personas
    ) {

        if (desde == null) {
            desde = LocalDate.now();
        }

        if (personas == null || personas < 1) {
            personas = 1;
        }

        Pageable pageable = PageRequest.of(
                0,
                3
        );

        return salidaRepository.buscarProximasSalidas(paqueteId, desde, personas, pageable)
                .stream()
                .map(salida -> new SalidaDTO(
                        salida.getId(),
                        salida.getFechaSalida(),
                        salida.getPrecio(),
                        salida.getCuposDisponibles()
                ))
                .toList();
    }

    public DisponibilidadDTO verificarDisponibilidad(
            Long paqueteId,
            LocalDate fechaSalida,
            Integer personas
    ) {

        if (personas == null || personas < 1) {
            personas = 1;
        }

        Salida salida = salidaRepository
                .buscarSalida(paqueteId, fechaSalida)
                .orElse(null);

        if (salida == null) {
            return new DisponibilidadDTO(
                    paqueteId,
                    null,
                    fechaSalida,
                    null,
                    0,
                    personas,
                    false
            );
        }

        boolean disponible =
                salida.getCuposDisponibles() >= personas;

        return new DisponibilidadDTO(
                paqueteId,
                salida.getPaquete().getNombre(),
                salida.getFechaSalida(),
                salida.getPrecio(),
                salida.getCuposDisponibles(),
                personas,
                disponible
        );
    }


}

