CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGSERIAL    PRIMARY KEY,
    usuario  VARCHAR(100) NOT NULL UNIQUE,
    correo   VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'CLIENTE'
);



CREATE TABLE IF NOT EXISTS destinos (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(160) NOT NULL,
    pais    VARCHAR(120) NOT NULL,
    ciudad  VARCHAR(120) NOT NULL,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL
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
    ultima_modificacion TIMESTAMP,
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


CREATE TABLE IF NOT EXISTS reservas (
    id                 BIGSERIAL PRIMARY KEY,
    usuario_id         BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    salida_id          BIGINT NOT NULL REFERENCES salidas (id) ON DELETE RESTRICT,
    fecha_reserva      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cantidad_personas  INTEGER NOT NULL,
    precio_unitario    NUMERIC(12, 2) NOT NULL,
    precio_total       NUMERIC(12, 2) NOT NULL,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones      VARCHAR(1000),

    CONSTRAINT ck_reservas_cantidad CHECK (cantidad_personas > 0),
    CONSTRAINT ck_reservas_precio_unitario CHECK (precio_unitario > 0),
    CONSTRAINT ck_reservas_precio_total CHECK (precio_total > 0),
    CONSTRAINT ck_reservas_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA'))
    );






CREATE UNIQUE INDEX IF NOT EXISTS uk_usuarios_correo ON usuarios (correo);

CREATE INDEX IF NOT EXISTS idx_paquetes_destino_id ON paquetes (destino_id);
CREATE INDEX IF NOT EXISTS idx_paquetes_disponible_destacado_nombre ON paquetes (disponible, destacado DESC, nombre);
CREATE INDEX IF NOT EXISTS idx_paquetes_destacado_nombre ON paquetes (destacado DESC, nombre);

CREATE INDEX IF NOT EXISTS idx_salidas_paquete_id ON salidas (paquete_id);
CREATE INDEX IF NOT EXISTS idx_salidas_paquete_fecha ON salidas (paquete_id, fecha_salida);

CREATE INDEX IF NOT EXISTS idx_paquete_imagenes_paquete_id ON paquete_imagenes (paquete_id);
CREATE INDEX IF NOT EXISTS idx_paquete_imagenes_paquete_orden ON paquete_imagenes (paquete_id, orden);
CREATE UNIQUE INDEX IF NOT EXISTS uk_paquete_imagenes_principal ON paquete_imagenes (paquete_id) WHERE principal = TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_contenidos_clave ON contenidos (clave);

CREATE INDEX IF NOT EXISTS idx_reservas_usuario_fecha ON reservas (usuario_id, fecha_reserva DESC);

CREATE INDEX IF NOT EXISTS idx_reservas_salida_estado ON reservas (salida_id, estado);

CREATE INDEX IF NOT EXISTS idx_reservas_estado_fecha ON reservas (estado, fecha_reserva DESC);