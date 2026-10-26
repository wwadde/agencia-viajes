package com.agencia.service;

import com.agencia.config.mapper.DestinoMapper;
import com.agencia.domain.Destino;
import com.agencia.domain.dto.DestinoDTO;
import com.agencia.exception.RecursoNoEncontradoException;
import com.agencia.repository.DestinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinoService {

    private final DestinoRepository destinoRepository;
    private final DestinoMapper destinoMapper;

    public List<DestinoDTO> listar() {
        return destinoRepository.findAll().stream()
                .sorted(Comparator.comparing(Destino::getNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Destino::getPais, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Destino::getCiudad, String.CASE_INSENSITIVE_ORDER))
                .map(destinoMapper::toDTO)
                .toList();
    }

    public DestinoDTO obtener(Long id) {
        return destinoMapper.toDTO(getByIdOrThrow(id));
    }

    public Long crear(DestinoDTO dto) {
        Destino destino = destinoMapper.toEntity(dto);
        return destinoRepository.save(destino).getId();
    }

    @Transactional
    public void actualizar(Long id, DestinoDTO dto) {
        Destino destino = getByIdOrThrow(id);
        destinoMapper.actualizar(dto, destino);
    }

    public void eliminar(Long id) {
        destinoRepository.delete(getByIdOrThrow(id));
    }

    private Destino getByIdOrThrow(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Destino no encontrado: " + id));
    }

}

