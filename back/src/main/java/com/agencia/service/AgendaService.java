package com.agencia.service;

import com.agencia.domain.Paquete;
import com.agencia.domain.Salida;
import com.agencia.domain.dto.AgendaSalidaDTO;
import com.agencia.domain.dto.AgendaSalidaUpsertDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.repository.PaqueteRepository;
import com.agencia.repository.SalidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    @Transactional
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
}

