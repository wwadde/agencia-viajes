package com.agencia.contenido.service;

import com.agencia.contenido.dto.ContenidoDTO;
import com.agencia.contenido.dto.ContenidoUpsertDTO;
import com.agencia.contenido.entity.Contenido;
import com.agencia.contenido.repository.ContenidoRepository;
import com.agencia.shared.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContenidoService {

    private final ContenidoRepository contenidoRepository;

    public List<ContenidoDTO> listar() {
        return contenidoRepository.findAll().stream()
                .sorted(Comparator.comparing(Contenido::getClave, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .toList();
    }

    public ContenidoDTO obtenerPorClave(String clave) {
        return toDTO(getByClaveOrThrow(clave));
    }

    public ContenidoDTO obtener(Long id) {
        return toDTO(getByIdOrThrow(id));
    }

    public Long crear(ContenidoUpsertDTO dto) {
        Contenido contenido = Contenido.builder().build();
        apply(contenido, dto);
        return contenidoRepository.save(contenido).getId();
    }

    @Transactional
    public void actualizar(Long id, ContenidoUpsertDTO dto) {
        Contenido contenido = getByIdOrThrow(id);
        apply(contenido, dto);
        contenidoRepository.save(contenido);
    }

    public void eliminar(Long id) {
        contenidoRepository.delete(getByIdOrThrow(id));
    }

    private Contenido getByIdOrThrow(Long id) {
        return contenidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado: " + id));
    }

    private Contenido getByClaveOrThrow(String clave) {
        String claveLimpia = clave == null ? "" : clave.trim();
        return contenidoRepository.findByClave(claveLimpia)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contenido no encontrado para clave: " + claveLimpia));
    }

    private void apply(Contenido contenido, ContenidoUpsertDTO dto) {
        contenido.setClave(dto.clave().trim());
        contenido.setValor(dto.valor());
    }

    private ContenidoDTO toDTO(Contenido contenido) {
        return new ContenidoDTO(
                contenido.getId(),
                contenido.getClave(),
                contenido.getValor()
        );
    }
}

