import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'paquetes/:slug',
    renderMode: RenderMode.Server
  },
  {
    path: 'foro',
    renderMode: RenderMode.Server
  },
  {
    path: 'foro/:slug',
    renderMode: RenderMode.Server
  },
  {
    path: 'login',
    renderMode: RenderMode.Client
  },
  {
    path: 'auth/google/callback',
    renderMode: RenderMode.Client
  },
  {
    path: 'mis-reservas/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'admin/**',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
