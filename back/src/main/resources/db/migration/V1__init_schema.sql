-- V1__init_schema.sql
-- Esquema inicial: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGSERIAL    PRIMARY KEY,
    usuario  VARCHAR(100) NOT NULL UNIQUE,
    correo   VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'CLIENTE'
);
