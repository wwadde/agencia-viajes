package com.agencia.outbox.service;

import com.agencia.chatbot.service.PaqueteVectorService;
import com.agencia.outbox.entity.EstadoEventoOutbox;
import com.agencia.outbox.entity.EventoOutbox;
import com.agencia.outbox.entity.TipoEntidadOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class GestionEventos {

    private final PaqueteVectorService paqueteVectorService;
    private final EventoOutboxService eventoOutboxService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void ejecutarEvento(EventoOutboxService.EventoOutboxCreado evento) {

        EventoOutbox outbox = evento.outbox();

        if (!eventoOutboxService.intentarTomarEvento(outbox.getId())) {
            log.debug("El evento {} ya está siendo procesado", outbox.getId());
            return;
        }

        try {

            if (outbox.getTipoEntidad() == TipoEntidadOutbox.PAQUETE) {

                boolean exitoso = paqueteVectorService.indexarPaquete(outbox.getEntidadId(), outbox.getTipoEvento());

                if (exitoso) {
                    eventoOutboxService.actualizarEstado(outbox.getId(), EstadoEventoOutbox.PROCESADO);

                    log.info("Evento Outbox procesado correctamente: {}", outbox.getId());

                } else {
                    eventoOutboxService.actualizarEstado(outbox.getId(), EstadoEventoOutbox.PENDIENTE);
                }
            }

        } catch (Exception e) {

            eventoOutboxService.actualizarEstado(outbox.getId(), EstadoEventoOutbox.PENDIENTE);

            log.error("Error procesando evento Outbox {}", outbox.getId(), e);
        }
    }


}
