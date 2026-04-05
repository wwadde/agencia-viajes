import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');
const backendTarget = process.env['BACKEND_URL'] ?? 'http://localhost:8080';

const app = express();
const angularApp = new AngularNodeAppEngine();

app.set('trust proxy', true);

// SSR en Node necesita alcanzar el backend para resolver rutas de paquetes en servidor.
app.use(
  createProxyMiddleware({
    pathFilter: ['/api/**', '/oauth2/**', '/login/oauth2/**', '/images/**', '/imagenes/**'],
    target: backendTarget,
    changeOrigin: true,
    xfwd: true,
    secure: false,
  }),
);

// Compatibilidad adicional para rutas base sin slash final.
app.use(
  ['/api', '/oauth2', '/login/oauth2', '/images', '/imagenes'],
  createProxyMiddleware({
    target: backendTarget,
    changeOrigin: true,
    xfwd: true,
    secure: false,
  }),
);

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
