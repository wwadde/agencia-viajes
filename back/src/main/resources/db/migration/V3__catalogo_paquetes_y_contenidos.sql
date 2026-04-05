-- V3__catalogo_paquetes_y_contenidos.sql
-- Estructura de catalogo para agencia de viajes: destinos, paquetes, salidas, imagenes y contenidos.

CREATE TABLE IF NOT EXISTS destinos (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(160) NOT NULL,
    pais    VARCHAR(120) NOT NULL,
    ciudad  VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS paquetes (
    id             BIGSERIAL PRIMARY KEY,
    nombre         VARCHAR(180) NOT NULL,
    slug           VARCHAR(240) NOT NULL UNIQUE,
    descripcion    VARCHAR(2000) NOT NULL,
    detalle        VARCHAR(5000) NOT NULL,
    duracion_dias  INTEGER NOT NULL,
    disponible     BOOLEAN NOT NULL DEFAULT FALSE,
    destacado      BOOLEAN NOT NULL DEFAULT FALSE,
    destino_id     BIGINT NOT NULL REFERENCES destinos (id)
);

CREATE TABLE IF NOT EXISTS salidas (
    id                 BIGSERIAL PRIMARY KEY,
    fecha_salida       DATE NOT NULL,
    precio             NUMERIC(12, 2) NOT NULL,
    cupos_disponibles  INTEGER NOT NULL,
    paquete_id         BIGINT NOT NULL REFERENCES paquetes (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS paquete_imagenes (
    id          BIGSERIAL PRIMARY KEY,
    url         VARCHAR(2048) NOT NULL,
    orden       INTEGER NOT NULL,
    principal   BOOLEAN NOT NULL DEFAULT FALSE,
    paquete_id  BIGINT NOT NULL REFERENCES paquetes (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contenidos (
    id      BIGSERIAL PRIMARY KEY,
    clave   VARCHAR(120) NOT NULL,
    valor   VARCHAR(5000)
);

-- Indices para consultas frecuentes y relaciones.
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_correo ON usuarios (correo);

CREATE INDEX IF NOT EXISTS idx_paquetes_destino_id ON paquetes (destino_id);
CREATE INDEX IF NOT EXISTS idx_paquetes_disponible_destacado_nombre ON paquetes (disponible, destacado DESC, nombre);
CREATE INDEX IF NOT EXISTS idx_paquetes_destacado_nombre ON paquetes (destacado DESC, nombre);

CREATE INDEX IF NOT EXISTS idx_salidas_paquete_id ON salidas (paquete_id);
CREATE INDEX IF NOT EXISTS idx_salidas_paquete_fecha ON salidas (paquete_id, fecha_salida);

CREATE INDEX IF NOT EXISTS idx_paquete_imagenes_paquete_id ON paquete_imagenes (paquete_id);
CREATE INDEX IF NOT EXISTS idx_paquete_imagenes_paquete_orden ON paquete_imagenes (paquete_id, orden);
CREATE UNIQUE INDEX IF NOT EXISTS uk_paquete_imagenes_principal
    ON paquete_imagenes (paquete_id)
    WHERE principal = TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_contenidos_clave ON contenidos (clave);

