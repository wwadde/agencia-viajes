-- V8__foro_moderacion_y_reacciones.sql
-- Agrega moderación de comentarios y reacciones de me gusta.

ALTER TABLE foro_comentarios
    ADD COLUMN IF NOT EXISTS oculto BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS foro_post_reacciones (
    id             BIGSERIAL PRIMARY KEY,
    post_id        BIGINT NOT NULL REFERENCES foro_posts (id) ON DELETE CASCADE,
    usuario_id     BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS foro_comentario_reacciones (
    id             BIGSERIAL PRIMARY KEY,
    comentario_id  BIGINT NOT NULL REFERENCES foro_comentarios (id) ON DELETE CASCADE,
    usuario_id     BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_foro_post_reacciones_post_usuario
    ON foro_post_reacciones (post_id, usuario_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_foro_comentario_reacciones_comentario_usuario
    ON foro_comentario_reacciones (comentario_id, usuario_id);

CREATE INDEX IF NOT EXISTS idx_foro_post_reacciones_post_id
    ON foro_post_reacciones (post_id);

CREATE INDEX IF NOT EXISTS idx_foro_comentario_reacciones_comentario_id
    ON foro_comentario_reacciones (comentario_id);

CREATE INDEX IF NOT EXISTS idx_foro_posts_titulo_lower
    ON foro_posts ((lower(titulo)));

CREATE INDEX IF NOT EXISTS idx_foro_posts_resumen_lower
    ON foro_posts ((lower(resumen)));
