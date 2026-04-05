package com.agencia.service;

import com.agencia.domain.Destino;
import com.agencia.domain.dto.DestinoDTO;
import com.agencia.domain.dto.DestinoUpsertDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.repository.DestinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinoService {

    private final DestinoRepository destinoRepository;

    public List<DestinoDTO> listar() {
        return destinoRepository.findAll().stream()
                .sorted(Comparator.comparing(Destino::getNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Destino::getPais, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Destino::getCiudad, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .toList();
    }

    public DestinoDTO obtener(Long id) {
        return toDTO(getByIdOrThrow(id));
    }

    @Transactional
    public Long crear(DestinoUpsertDTO dto) {
        Destino destino = Destino.builder().build();
        apply(destino, dto);
        return destinoRepository.save(destino).getId();
    }

    @Transactional
    public void actualizar(Long id, DestinoUpsertDTO dto) {
        Destino destino = getByIdOrThrow(id);
        apply(destino, dto);
        destinoRepository.save(destino);
    }

    @Transactional
    public void eliminar(Long id) {
        destinoRepository.delete(getByIdOrThrow(id));
    }

    private Destino getByIdOrThrow(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Destino no encontrado: " + id));
    }

    private void apply(Destino destino, DestinoUpsertDTO dto) {
        destino.setNombre(dto.nombre().trim());
        destino.setPais(dto.pais().trim());
        destino.setCiudad(dto.ciudad().trim());
    }

    private DestinoDTO toDTO(Destino destino) {
        return new DestinoDTO(
                destino.getId(),
                destino.getNombre(),
                destino.getPais(),
                destino.getCiudad()
        );
    }
}

