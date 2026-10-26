package com.agencia.outbox.entity;

public enum EstadoEventoOutbox {
    PENDIENTE,
    PROCESANDO,
    PROCESADO,
    FALLIDO,
    CANCELADO
}
