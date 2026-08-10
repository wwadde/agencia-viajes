package com.agencia.domain;

import com.agencia.utils.EstadoEventoOutbox;
import com.agencia.utils.TipoEntidadOutbox;
import com.agencia.utils.TipoEventoOutbox;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad")
    private TipoEntidadOutbox tipoEntidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento")
    private TipoEventoOutbox tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoEventoOutbox estado = EstadoEventoOutbox.PENDIENTE;

    @Column(name = "intentos")
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "procesado_en")
    private LocalDateTime procesadoEn;

    @Column(name = "ultimo_error")
    private String ultimoError;

}
