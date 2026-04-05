FROM node:22-alpine AS builder

RUN corepack enable && corepack prepare pnpm@9.1.4 --activate

WORKDIR /app

COPY pnpm-lock.yaml package.json ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

FROM node:22-alpine AS runtime

RUN apk add --no-cache nginx

WORKDIR /app

LABEL org.opencontainers.image.title="agencia-frontend"
LABEL org.opencontainers.image.description="Angular hybrid rendering (SSR + CSR) – Agencia de viajes"

COPY nginx.conf /etc/nginx/http.d/default.conf

COPY --from=builder /app/dist/agencia-front/browser /usr/share/nginx/html
COPY --from=builder /app/dist/agencia-front/server /app/dist/agencia-front/server

ENV PORT=4000
ENV BACKEND_URL=http://fullstack-baken-t8b9t3:8080

EXPOSE 80

CMD ["sh", "-c", "node /app/dist/agencia-front/server/server.mjs & nginx -g 'daemon off;'"]