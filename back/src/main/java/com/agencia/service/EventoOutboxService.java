package com.agencia.service;

import com.agencia.domain.EventoOutbox;
import com.agencia.repository.EventoOutboxRepository;
import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import com.agencia.utils.TipoEventoOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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

        var entidad = repository.save(evento);

        applicationEventPublisher.publishEvent(new EventoOutboxCreado(entidad));
    }


    public record EventoOutboxCreado(EventoOutbox outbox){}


    public boolean intentarTomarEvento(UUID id) {

        return repository.intentarTomarEvento(id, EstadoEventoOutbox.PENDIENTE,
                EstadoEventoOutbox.PROCESANDO,
                LocalDateTime.now()
        ) == 1;
    }


}
