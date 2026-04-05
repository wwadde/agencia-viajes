-- V2__usuario_rol.sql
-- Agrega la columna rol a la tabla usuarios.
-- Los usuarios existentes reciben el rol por defecto CLIENTE.

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS rol VARCHAR(50) NOT NULL DEFAULT 'CLIENTE';

