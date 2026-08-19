-- V3__reservas.sql
-- Gestion de reservas de clientes por salida con control de cupos.

CREATE TABLE IF NOT EXISTS reservas (
    id                 BIGSERIAL PRIMARY KEY,
    usuario_id         BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    salida_id          BIGINT NOT NULL REFERENCES salidas (id) ON DELETE RESTRICT,
    fecha_reserva      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cantidad_personas  INTEGER NOT NULL,
    precio_unitario    NUMERIC(12, 2) NOT NULL,
    precio_total       NUMERIC(12, 2) NOT NULL,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones      VARCHAR(1000),

    CONSTRAINT ck_reservas_cantidad CHECK (cantidad_personas > 0),
    CONSTRAINT ck_reservas_precio_unitario CHECK (precio_unitario > 0),
    CONSTRAINT ck_reservas_precio_total CHECK (precio_total > 0),
    CONSTRAINT ck_reservas_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_reservas_usuario_fecha
    ON reservas (usuario_id, fecha_reserva DESC);

CREATE INDEX IF NOT EXISTS idx_reservas_salida_estado
    ON reservas (salida_id, estado);

CREATE INDEX IF NOT EXISTS idx_reservas_estado_fecha
    ON reservas (estado, fecha_reserva DESC);

