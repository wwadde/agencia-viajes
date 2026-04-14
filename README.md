# Agencia Viajes Monorepo

Monorepo de la aplicacion de agencia de viajes con frontend Angular y backend Spring Boot.

## Estructura

- `front/`: app Angular (SSR + CSR), panel admin y experiencia publica.
- `back/`: API Spring Boot, seguridad por sesion, JPA/Flyway, pagos y carga de imagenes.
- `.github/workflows/`: CI para construir y publicar imagenes Docker por servicio.

## Stack

- Frontend: Angular 21, TypeScript, Vitest, Node 22, pnpm 9.
- Backend: Spring Boot 4, Java 25, Gradle, PostgreSQL, Flyway.
- Infra: Docker, Nginx reverse proxy, GHCR para imagenes.

## Requisitos

- Node.js 22+
- pnpm 9.1.4
- Java 25
- Docker (opcional para contenedores)
- PostgreSQL 14+

## Primer arranque local

### 1) Backend

Desde `back/`:

```bash
./gradlew.bat bootRun
```

Variables minimas para backend:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Nota: la app importa opcionalmente `.env` con `spring.config.import=optional:file:.env[.properties]`.

API local por defecto: `http://localhost:8080`

### 2) Frontend

Desde `front/`:

```bash
pnpm install
pnpm start
```

App local por defecto: `http://localhost:4200`

El proxy de desarrollo envia `/api`, `/oauth2`, `/login/oauth2`, `/images` e `/imagenes` al backend (`http://localhost:8080`).

## Comandos utiles

### Frontend (`front/`)

```bash
pnpm start      # desarrollo
pnpm build      # build produccion
pnpm test       # unit tests
pnpm serve:ssr  # servir salida SSR ya construida
```

### Backend (`back/`)

```bash
./gradlew.bat test                 # tests
./gradlew.bat bootRun              # desarrollo
./gradlew.bat bootJar -x test      # empaquetado jar
```

## Docker

### Backend

Desde `back/`:

```bash
docker build -t agencia-backend .
```

### Frontend

Desde `front/`:

```bash
docker build -t agencia-frontend .
```

## CI/CD

- `backdeploy.yml`: build/push de imagen backend cuando hay cambios en `back/**`.
- `frontdeploy.yml`: build/push de imagen frontend cuando hay cambios en `front/**`.
- Ambas pipelines publican en GHCR.
- Accedemos al panel de dokploy mediante proxy tunel de cloudflare para no exponer puerto 3000

## Convenciones de trabajo en este monorepo

- Usar `pnpm` solo en frontend.
- Mantener contratos API alineados entre `front/src/app/core/models/` y DTOs del backend.
- Preservar integracion por cookie de sesion (`withCredentials`) entre frontend y backend.
- Usar migraciones Flyway para cambios de esquema en backend.
- Evitar hardcodear hosts en frontend; usar configuracion por entorno.
