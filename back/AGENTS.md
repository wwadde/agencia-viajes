# AGENTS Guide for `agencia`

## Product and deployment context
- Product scope: travel packages + scheduled departures ("agendas") + admin panel for dynamic site content; payment gateway integration includes PayU WebCheckout sandbox base flow.
- Production topology: Angular is exposed by Nginx on a VPS, Spring Boot runs behind it and is reached through reverse proxy.
- Image strategy is intentionally server-local: upload from admin panel, persist on backend disk, serve statically via Nginx (expected volume is low, <25 images).

## What this project is
- Spring Boot 4 backend for a travel agency API (`build.gradle.kts`) using MVC + Security + JPA + Flyway.
- Runtime is session-cookie auth (no JWT): login writes Spring Security context into `HttpSession` (`src/main/java/com/agencia/auth/service/AuthService.java`).
- Code is organized by feature under `src/main/java/com/agencia/<feature>/` (`auth`, `destino`, `paquete`, `agenda`, `reserva`, `pago`, `contenido`, `foro`, `home`, `sitemap`, `imagen`, `outbox`, `chatbot`), each with its own `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `exception`, `utils` subpackages as needed.
- Cross-cutting code (security, global exception handler, schedulers, shared exceptions) lives in `src/main/java/com/agencia/shared`.

## Architecture and request flow
- Layering inside each feature is `controller -> service -> repository -> entity` (see `auth`: `AuthController`, `AuthService`, `UsuarioRepository`, `Usuario`).
- Auth flow: `POST /api/auth/login` authenticates credentials, persists context via `HttpSessionSecurityContextRepository`, then frontend reuses cookie (`src/main/java/com/agencia/auth/controller/AuthController.java`).
- Google OAuth2 flow: Spring Security handles `/oauth2/**` and `/login/oauth2/**`; on success `OAuth2LoginSuccessHandler` provisions/fetches the local user via `GoogleUserProvisioningService`, saves `SecurityContext` in session, then redirects to frontend callback path with `status` query param (`src/main/java/com/agencia/shared/config/security/OAuth2LoginSuccessHandler.java`).
- File flow: images are uploaded first via `ImagenController` (`/api/admin/imagenes`) with `multipart/form-data` + logical folder (`paquetes`, `iconos`, `landing`); `StorageService`/`FileSystemStorageService` validates MIME/signature/size and writes to local disk in that folder, returning a relative reference (e.g. `paquetes/banner-abc123.jpg`).
- Packages flow: `PaqueteController` (public read endpoints) and `AdminPaqueteController` (admin CRUD JSON) both use `PaqueteService` + JPA repositories; package image fields store only relative references returned by the image API.
- Agendas flow: `AgendaController` exposes upcoming departures for public listing/filter by package; `AdminAgendaController` handles full agenda CRUD for backoffice content operations.
- Destinos flow: `DestinoController` exposes public catalog reads (`/api/destinos`), while `AdminDestinoController` manages admin CRUD (`/api/admin/destinos`) through `DestinoService`.
- Contenidos flow: `ContenidoController` exposes public content reads by `clave` (`/api/contenidos`), while `AdminContenidoController` manages admin CRUD (`/api/admin/contenidos`) through `ContenidoService`.
- Reservas flow: `ReservaController` allows authenticated `CLIENTE` users to create, list, view and cancel their own reservations; `AdminReservaController` lets admins list reservations and change reservation status.
- Pagos flow: `ReservaController` can create payment attempts and list payment attempts for a reservation; `PagoWebhookController` receives PayU callbacks (`/api/pagos/webhook/payu` form-urlencoded);
  `PagoService` manages state transitions + reservation confirmation, and `PayUClient` builds WebCheckout payload/signature. PayU is the only payment gateway.

## Security model and error contract
- `WebSecurity` permits auth bootstrap endpoints (`/api/auth/login`, `/api/auth/register`, `/api/auth/logout`, `/api/auth/csrf`), OAuth2 entry/callback (`/oauth2/**`, `/login/oauth2/**`), payment webhooks, public catalog routes (`/api/paquetes/**`, `/api/destinos/**`, `/api/contenidos/**`, `/api/agendas/**`) and Swagger; everything else requires authentication (`src/main/java/com/agencia/shared/config/WebSecurity.java`).
- CORS is configured from `app.cors.allowed-origins` and `allowCredentials=true`; session cookies are expected across origins.
- CSRF is enabled with SPA support and explicitly ignored only for the PayU webhook endpoint (`/api/pagos/webhook/payu`).
- Admin endpoints use method security with authority checks (use `hasAuthority('ADMIN')` with current `Rol` enum values).
- API errors are RFC 7807 `ProblemDetail` in two places:
  - MVC exceptions via `GlobalExceptionHandler`.
  - Security filter exceptions via `CustomAuthenticationEntryPoint` / `CustomAccessDeniedHandler` + `ProblemDetailWriter`.

## Data and migrations
- DB is PostgreSQL from env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) in `src/main/resources/application.yaml`.
- Catalog images are modeled as entity rows (`ImagenPaquete`) with `url`, `orden`, `principal`; store only local relative references in DB (prefer `carpeta/archivo.ext`, never absolute HTTP URL) and build public URL at response time.
- Reservas persist a snapshot of prices (`precio_unitario`, `precio_total`) and use `estado` (`PENDIENTE`, `CONFIRMADA`, `CANCELADA`) with cupo updates done transactionally.
- Pagos use a dedicated `pagos` table linked to `reservas`, with states (`PENDIENTE`, `REQUIERE_ACCION`, `APROBADO`, `RECHAZADO`, `CANCELADO`, `EXPIRADO`) and provider (`PAYU`; the DB check constraint from `V2__pagos.sql` still allows legacy values).
- `Usuario.rol` uses enum values `ADMIN` / `CLIENTE` (no explicit `ROLE_` prefix in `Rol`).

## Developer workflows (Windows PowerShell)
- Local run with dev profile (default from `application.yaml`): `./gradlew.bat bootRun`
- Tests: `./gradlew.bat test`
- Jar build used by container stage: `./gradlew.bat bootJar -x test`
- Docker image build (multi-stage Java 25): `docker build -t agencia-backend .`
- CI pushes image to GHCR on `main` via `.github/workflows/deploy.yml`.

## Project-specific conventions to follow
- Prefer Java `record` DTOs for API contracts, placed in the owning feature's `dto` package (e.g. `src/main/java/com/agencia/paquete/dto`).
- Validation messages and API texts are Spanish; preserve language consistency when adding fields/errors.
- Keep `ProblemDetail` shape consistent (`title`, `detail`, `type`, `timestamp`, optional `violations`).
- For file upload features, preserve current hardening checks (safe path resolution, magic bytes, MIME allowlist, size limits).
- Build incrementally: implement APIs in small vertical slices (public read -> admin write -> validation/security) to match product rollout.

## PayU WebCheckout notes (sandbox)
- Credentials are static per comercio (`merchantId`, `accountId`, `apiKey`); what changes on each intento is `referenceCode`, `amount` and therefore the generated `signature`.
- Signature used in checkout is built as `apiKey~merchantId~referenceCode~amount~currency` (MD5 currently).
- For sandbox testing, enforce minimum per-currency thresholds configured in `payments.payu.sandbox-min-amount-by-currency` (default includes `USD` and `COP`).
- WebCheckout integration pattern for Angular: backend returns `urlCheckout` + payload with form fields; frontend should submit an HTML form `POST` to PayU (not a JSON fetch).
- PayU confirmation callback must point to a public backend URL (`payments.payu.confirmation-url`) reachable from internet/Nginx proxy.
- Keep `referenceCode` alphanumeric and unique per intento; never reuse a paid reference.

## AI-instruction sources scanned
- Glob scanned: `**/{.github/copilot-instructions.md,AGENT.md,AGENTS.md,CLAUDE.md,.cursorrules,.windsurfrules,.clinerules,.cursor/rules/**,.windsurf/rules/**,.clinerules/**,README.md}`.
- Result at scan time: found `AGENTS.md` at project root; no additional AI-instruction files matched.
