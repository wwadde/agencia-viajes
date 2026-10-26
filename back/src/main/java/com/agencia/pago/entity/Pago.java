package com.agencia.pago.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.agencia.reserva.entity.Reserva;

@Entity
@Table(name = "pagos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PagoEstado estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PagoProveedor proveedor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "referencia_externa", length = 160)
    private String referenciaExterna;

    @Column(name = "url_checkout", length = 2048)
    private String urlCheckout;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "payload_pasarela", columnDefinition = "TEXT")
    private String payloadPasarela;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Column(name = "confirmado_en")
    private LocalDateTime confirmadoEn;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;
}

