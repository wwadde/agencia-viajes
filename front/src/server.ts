import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';

const browserDistFolder = join(import.meta.dirname, '../browser');
const backendTarget = process.env['BACKEND_URL'] ?? 'http://localhost:8080';

const app = express();
const angularApp = new AngularNodeAppEngine();
const proxyPathPrefixes = ['/api', '/oauth2', '/login/oauth2', '/images', '/imagenes'];

app.set('trust proxy', true);

app.use((req, res, next) => {
  if (!shouldProxyPath(req.path)) {
    return next();
  }

  proxyToBackend(req, res).catch(next);
});

function shouldProxyPath(pathname: string): boolean {
  return proxyPathPrefixes.some((prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`));
}

async function proxyToBackend(req: express.Request, res: express.Response): Promise<void> {
  const targetUrl = new URL(req.originalUrl, backendTarget);
  const headers = buildProxyHeaders(req);
  const method = req.method.toUpperCase();
  const requestInit: RequestInit & { duplex?: 'half' } = {
    method,
    headers,
    redirect: 'manual',
  };

  if (method !== 'GET' && method !== 'HEAD') {
    requestInit.body = req as unknown as BodyInit;
    requestInit.duplex = 'half';
  }

  const response = await fetch(targetUrl, requestInit);
  res.status(response.status);

  response.headers.forEach((value, key) => {
    const header = key.toLowerCase();
    if (header === 'content-length' || header === 'connection' || header === 'transfer-encoding') {
      return;
    }
    res.setHeader(key, value);
  });

  if (!response.body) {
    res.end();
    return;
  }

  await pipeline(Readable.fromWeb(response.body as any), res);
}

function buildProxyHeaders(req: express.Request): Headers {
  const headers = new Headers();

  Object.entries(req.headers).forEach(([name, value]) => {
    if (name === 'host' || name === 'connection' || name === 'content-length') {
      return;
    }

    if (Array.isArray(value)) {
      headers.set(name, value.join(', '));
      return;
    }

    if (typeof value === 'string') {
      headers.set(name, value);
    }
  });

  const clientIp = req.ip ?? req.socket.remoteAddress ?? '';
  const forwardedFor = req.headers['x-forwarded-for'];
  const xForwardedFor = Array.isArray(forwardedFor)
    ? forwardedFor.join(', ')
    : forwardedFor;

  if (clientIp) {
    headers.set('x-forwarded-for', xForwardedFor ? `${xForwardedFor}, ${clientIp}` : clientIp);
  }
  headers.set('x-forwarded-proto', req.protocol);

  const host = req.get('host');
  if (host) {
    headers.set('x-forwarded-host', host);
  }

  return headers;
}

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
