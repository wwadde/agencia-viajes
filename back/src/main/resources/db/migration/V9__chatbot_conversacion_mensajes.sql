CREATE TABLE conversaciones
(
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT       NOT NULL,
    titulo              VARCHAR(255) NOT NULL,
    fecha_creacion      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fecha_actualizacion TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT fk_conversaciones_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuarios (id)
            ON DELETE CASCADE
);

CREATE TABLE mensajes_chat
(
    id              BIGSERIAL PRIMARY KEY,
    conversacion_id BIGINT      NOT NULL,
    mensaje         TEXT        NOT NULL,
    rol_mensaje     VARCHAR(20) NOT NULL,
    fecha_hora      TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    CONSTRAINT fk_mensajes_chat_conversacion
        FOREIGN KEY (conversacion_id)
            REFERENCES conversaciones (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_mensajes_chat_rol
        CHECK (rol_mensaje IN ('USUARIO', 'CHATBOT', 'SYSTEM'))
);

-- Conversaciones de un usuario
CREATE INDEX idx_conversaciones_usuario
    ON conversaciones (usuario_id);

-- Listar conversaciones recientes
CREATE INDEX idx_conversaciones_usuario_fecha
    ON conversaciones (usuario_id, fecha_actualizacion DESC);

-- Obtener todos los mensajes de una conversación
CREATE INDEX idx_mensajes_chat_conversacion
    ON mensajes_chat (conversacion_id);

-- Obtener los mensajes ordenados cronológicamente
CREATE INDEX idx_mensajes_chat_conversacion_fecha
    ON mensajes_chat (conversacion_id, fecha_hora);

-- Si en un futuro buscas por fecha
CREATE INDEX idx_mensajes_chat_fecha
    ON mensajes_chat (fecha_hora);