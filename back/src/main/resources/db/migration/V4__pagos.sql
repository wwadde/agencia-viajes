-- V4__pagos.sql
-- Estructura base para pagos de reservas e integracion futura con pasarela.

CREATE TABLE IF NOT EXISTS pagos (
    id                  BIGSERIAL PRIMARY KEY,
    reserva_id          BIGINT NOT NULL REFERENCES reservas (id) ON DELETE CASCADE,
    estado              VARCHAR(30) NOT NULL,
    proveedor           VARCHAR(30) NOT NULL,
    monto               NUMERIC(12, 2) NOT NULL,
    moneda              VARCHAR(3) NOT NULL,
    referencia_externa  VARCHAR(160),
    url_checkout        VARCHAR(2048),
    idempotency_key     VARCHAR(120),
    payload_pasarela    TEXT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    confirmado_en       TIMESTAMPTZ,
    expira_en           TIMESTAMPTZ,

    CONSTRAINT ck_pagos_estado CHECK (estado IN ('PENDIENTE', 'REQUIERE_ACCION', 'APROBADO', 'RECHAZADO', 'CANCELADO', 'EXPIRADO')),
    CONSTRAINT ck_pagos_proveedor CHECK (proveedor IN ('SIN_CONFIGURAR', 'MERCADOPAGO', 'STRIPE', 'PAYU')),
    CONSTRAINT ck_pagos_monto CHECK (monto > 0)
);

CREATE INDEX IF NOT EXISTS idx_pagos_reserva_creado
    ON pagos (reserva_id, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_pagos_estado_actualizado
    ON pagos (estado, actualizado_en DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uk_pagos_referencia_externa
    ON pagos (referencia_externa)
    WHERE referencia_externa IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_pagos_idempotency_key
    ON pagos (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

