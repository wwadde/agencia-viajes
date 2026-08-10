package com.agencia.config;

import com.agencia.chatbot.service.PaqueteVectorService;
import com.agencia.domain.EventoOutbox;
import com.agencia.repository.EventoOutboxRepository;
import com.agencia.service.EventoOutboxService;
import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import com.agencia.utils.TipoEventoOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestionEventos {

    private final EventoOutboxRepository eventoOutboxRepository;
    private final PaqueteVectorService paqueteVectorService;
    private final EventoOutboxService eventoOutboxService;

    @Async
    @EventListener(EventoOutboxService.EventoOutboxCreado.class)
    public void ejecutarEvento(EventoOutboxService.EventoOutboxCreado evento) {

        EventoOutbox outbox = evento.outbox();

        if (!eventoOutboxService.intentarTomarEvento(outbox.getId())) {
            log.debug("El evento {} ya está siendo procesado", outbox.getId());
            return;
        }

        try {

            if (outbox.getTipoEntidad() == TipoEntidadOutbox.PAQUETE) {

                boolean exitoso = paqueteVectorService.indexarPaquete(outbox.getEntidadId(), TipoEventoOutbox.CREADO);

                if (exitoso) {
                    eventoOutboxRepository.actualizar(outbox.getId(), EstadoEventoOutbox.PROCESADO, LocalDateTime.now());

                    log.info("Evento Outbox procesado correctamente: {}", outbox.getId());

                } else {
                    eventoOutboxRepository.actualizar(outbox.getId(), EstadoEventoOutbox.PENDIENTE, LocalDateTime.now());
                }
            }

        } catch (Exception e) {

            eventoOutboxRepository.actualizar(outbox.getId(), EstadoEventoOutbox.PENDIENTE, LocalDateTime.now());

            log.error("Error procesando evento Outbox {}", outbox.getId(), e);
        }
    }


}
