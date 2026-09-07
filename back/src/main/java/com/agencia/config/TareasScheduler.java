package com.agencia.config;

import com.agencia.chatbot.ChatMemoryManager;
import com.agencia.chatbot.service.PaqueteVectorService;
import com.agencia.domain.EventoOutbox;
import com.agencia.repository.EventoOutboxRepository;
import com.agencia.service.EventoOutboxService;
import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TareasScheduler {


    private final ChatMemory chatMemory;
    private final ChatMemoryManager chatMemoryManager;
    private final EventoOutboxRepository eventoOutboxRepository;
    private final PaqueteVectorService paqueteVectorService;
    private final EventoOutboxService eventoOutboxService;

    @Async
    @Scheduled(cron = "0 5 0 * * ?")
    public void limpiarMemoriaChatbot() {
        log.info("Scheduler limpiando chatbot");

        chatMemoryManager.limpiar();
    }

    @Async
    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.HOURS)
    public void gestionarEventosOutbox() {
        log.info("Scheduler gestionando eventos");

        List<EventoOutbox> eventos = eventoOutboxRepository.findTop20ByEstadoOrderByCreadoEnAsc(EstadoEventoOutbox.PENDIENTE);
        if (eventos.isEmpty()) {
            return;
        }
        
        for (EventoOutbox evento : eventos) {

            if (evento.getTipoEntidad().equals(TipoEntidadOutbox.PAQUETE)){
                boolean exitoso = paqueteVectorService.indexarPaquete(evento.getEntidadId(), evento.getTipoEvento());
                if (exitoso) {

                    eventoOutboxService.actualizarEstado(evento.getId(), EstadoEventoOutbox.PROCESADO);
                    log.info("Evento Outbox procesado correctamente: {}", evento.getId());
                }
            }

        }

    }


}
