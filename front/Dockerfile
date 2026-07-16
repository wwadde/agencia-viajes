FROM node:22-alpine AS builder

RUN corepack enable && corepack prepare pnpm@9.1.4 --activate

WORKDIR /app

COPY pnpm-lock.yaml package.json ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

FROM node:22-alpine AS runtime

RUN apk add --no-cache nginx gettext

WORKDIR /app

LABEL org.opencontainers.image.title="agencia-frontend"
LABEL org.opencontainers.image.description="Angular hybrid rendering (SSR + CSR) – Agencia de viajes"

COPY nginx.conf.template /etc/nginx/templates/default.conf.template

COPY --from=builder /app/dist/agencia-front/browser /usr/share/nginx/html
COPY --from=builder /app/dist/agencia-front/server /app/dist/agencia-front/server

ENV PORT=4000

EXPOSE 80

CMD ["sh", "-c", "envsubst '$BACKEND_URL' < /etc/nginx/templates/default.conf.template > /etc/nginx/http.d/default.conf && node /app/dist/agencia-front/server/server.mjs & nginx -g 'daemon off;'"]