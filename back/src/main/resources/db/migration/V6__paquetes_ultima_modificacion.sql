-- V6__paquetes_ultima_modificacion.sql
-- Agrega marca temporal para cambios de paquetes y soportar lastmod en sitemap.

ALTER TABLE paquetes
    ADD COLUMN IF NOT EXISTS ultima_modificacion TIMESTAMP;

UPDATE paquetes
SET ultima_modificacion = NOW()
WHERE ultima_modificacion IS NULL;

ALTER TABLE paquetes
    ALTER COLUMN ultima_modificacion SET NOT NULL;

