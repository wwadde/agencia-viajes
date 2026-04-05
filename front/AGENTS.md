# Project Guidelines

This file is the canonical workspace instruction source for this repository.

## Build and Test
- Use pnpm only (`pnpm@9.1.4`).
- Install deps: `pnpm install`.
- Dev server: `pnpm start`.
- Production build: `pnpm build`.
- Unit tests (Vitest): `pnpm test`.
- Run SSR output after build: `pnpm serve:ssr`.

## Architecture
- Angular v21+ hybrid SSR app.
- Public SEO route `/paquetes/:slug` is server-rendered; auth/admin/reservas routes are client-rendered.
- Core app composition:
  - `src/app/core/guards/` for route authorization.
  - `src/app/core/interceptors/` for cross-cutting HTTP concerns.
  - `src/app/core/services/` for signal-based domain services.
  - `src/app/core/models/` for DTO interfaces aligned with backend contract.
  - `src/app/features/` for route-focused feature areas.
- Deployment topology:
  - Nginx serves static assets and proxies `/api` to Spring Boot.
  - Angular SSR Node runtime serves app rendering behind Nginx.

## Conventions
- Angular standards:
  - Standalone components only.
  - Do not add `standalone: true` to decorators.
  - Use `input()` and `output()` instead of decorator inputs/outputs.
  - Use `inject()` instead of constructor injection.
  - Always set `ChangeDetectionStrategy.OnPush`.
  - Use native control flow (`@if`, `@for`, `@switch`), never structural `*` directives.
  - Avoid `ngClass` and `ngStyle`; use class/style bindings.
  - Prefer reactive forms.
- State and services:
  - Use signals for local and service state.
  - Expose read-only signals with `.asReadonly()`.
  - Prefer `firstValueFrom()` inside async service methods.
- UX and validation:
  - Keep user-facing copy and errors in Spanish.
  - Handle backend RFC 7807 errors through `ProblemDetail` (`src/app/core/models/error.model.ts`).

## Critical Gotchas
- Auth is session-cookie based (not JWT). Keep `withCredentials: true` behavior intact.
- Do not hardcode backend hosts in services; use `environment.apiUrl` (relative `/api`).
- PayU checkout must submit a native hidden HTML form from `checkoutPayload`; do not use `HttpClient` or `fetch` for that redirect POST.
- Package/admin image references are relative; construct final URLs through shared image URL helpers/pipes, not hardcoded paths.
- Keep SSR routing assumptions in sync with `src/app/app.routes.server.ts`, `src/server.ts`, and `nginx.conf`.

## Documentation Map
- Coding standards: `best-practices.md`
- API contract: `api-docs.json`
- Core routing: `src/app/app.routes.ts`
- Server render modes: `src/app/app.routes.server.ts`
- Auth/session behavior: `src/app/core/services/auth.service.ts` and `src/app/core/interceptors/auth.interceptor.ts`
- Deployment and proxy behavior: `nginx.conf`, `Dockerfile`, `src/server.ts`, `proxy.conf.json`
- Skills directory: `.agents/skills/`

## Skills
Read the relevant skill before implementation when applicable:
- `.agents/skills/angular-component/SKILL.md`
- `.agents/skills/angular-signals/SKILL.md`
- `.agents/skills/frontend-design/SKILL.md`
- `.agents/skills/tailwind-design-system/SKILL.md`

## Skills Workflow (Required)
- If the task matches a skill domain, read that `SKILL.md` first and then implement.
- If a task spans domains, read multiple skills before coding.
- Use this mapping:
  - Component creation/refactor: `angular-component`
  - Signal state or service reactivity: `angular-signals`
  - UI page/component visual design: `frontend-design`
  - Tailwind tokens/system/component library work: `tailwind-design-system`
- For small edits that do not match any skill domain, proceed without forcing a skill.
