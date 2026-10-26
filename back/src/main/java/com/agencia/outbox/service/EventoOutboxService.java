package com.agencia.service;

import com.agencia.domain.EventoOutbox;
import com.agencia.repository.EventoOutboxRepository;
import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import com.agencia.utils.TipoEventoOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoOutboxService {

    private final EventoOutboxRepository repository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void registrar(TipoEntidadOutbox tipoEntidad, Long entidadId, TipoEventoOutbox tipoEvento) {
        EventoOutbox evento = EventoOutbox.builder()
                .tipoEntidad(tipoEntidad)
                .entidadId(entidadId)
                .tipoEvento(tipoEvento)
                .estado(EstadoEventoOutbox.PENDIENTE)
                .intentos(0)
                .creadoEn(LocalDateTime.now())
                .build();

        // Cancelar cualquier evento pendiente o procesando existente para la misma entidad
        repository.encontrarSegunTipo(tipoEntidad, entidadId, EstadoEventoOutbox.PROCESANDO, EstadoEventoOutbox.PENDIENTE).ifPresent(eventoExiste -> {
            eventoExiste.setEstado(EstadoEventoOutbox.CANCELADO);
            eventoExiste.setProcesadoEn(LocalDateTime.now());
            repository.save(eventoExiste);
        });


        var entidad = repository.save(evento);

        applicationEventPublisher.publishEvent(new EventoOutboxCreado(entidad));
    }


    public record EventoOutboxCreado(EventoOutbox outbox){}


    @Transactional
    public boolean intentarTomarEvento(UUID id) {

        return repository.intentarTomarEvento(id, EstadoEventoOutbox.PENDIENTE,
                EstadoEventoOutbox.PROCESANDO,
                LocalDateTime.now()
        ) == 1;
    }

    @Transactional
    public void actualizarEstado(UUID id, EstadoEventoOutbox estado) {
        repository.actualizar(id, estado, LocalDateTime.now());
    }





}
