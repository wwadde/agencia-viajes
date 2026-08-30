-- V3__foro_posts_y_comentarios.sql
-- Agrega módulo de foro para contenido SEO y engagement.

CREATE TABLE IF NOT EXISTS foro_posts (
    id                  BIGSERIAL PRIMARY KEY,
    titulo              VARCHAR(180) NOT NULL,
    slug                VARCHAR(240) NOT NULL UNIQUE,
    resumen             VARCHAR(2000) NOT NULL,
    publicado           BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_publicacion   TIMESTAMP,
    ultima_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
    autor_id            BIGINT NOT NULL REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS foro_post_bloques (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES foro_posts (id) ON DELETE CASCADE,
    tipo        VARCHAR(20) NOT NULL,
    contenido   VARCHAR(5000) NOT NULL,
    pie_imagen  VARCHAR(240),
    orden       INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT ck_foro_post_bloques_tipo CHECK (tipo IN ('TEXTO', 'IMAGEN'))
);

CREATE TABLE IF NOT EXISTS foro_comentarios (
    id             BIGSERIAL PRIMARY KEY,
    post_id        BIGINT NOT NULL REFERENCES foro_posts (id) ON DELETE CASCADE,
    usuario_id     BIGINT NOT NULL REFERENCES usuarios (id),
    contenido      VARCHAR(1500) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_foro_posts_publicado_fecha ON foro_posts (publicado, fecha_publicacion DESC);
CREATE INDEX IF NOT EXISTS idx_foro_posts_fecha_modificacion ON foro_posts (ultima_modificacion DESC);
CREATE INDEX IF NOT EXISTS idx_foro_post_bloques_post_orden ON foro_post_bloques (post_id, orden);
CREATE INDEX IF NOT EXISTS idx_foro_comentarios_post_fecha ON foro_comentarios (post_id, fecha_creacion, id);
