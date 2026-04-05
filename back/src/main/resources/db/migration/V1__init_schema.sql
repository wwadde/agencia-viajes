-- V1__init_schema.sql
-- Esquema inicial: usuarios y refresh_tokens
CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGSERIAL    PRIMARY KEY,
    usuario  VARCHAR(100) NOT NULL UNIQUE,
    correo   VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    usuario_id BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_token      ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_usuario_id ON refresh_tokens(usuario_id);
