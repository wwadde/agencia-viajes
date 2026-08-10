CREATE TABLE eventos_outbox (
    id UUID PRIMARY KEY,
    tipo_entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INTEGER NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    procesado_en TIMESTAMP,
    ultimo_error TEXT
);

CREATE INDEX idx_eventos_outbox_pendientes
    ON eventos_outbox (estado, creado_en);