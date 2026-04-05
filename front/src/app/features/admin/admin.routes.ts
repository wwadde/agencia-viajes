import { Routes } from '@angular/router';

export default [
  {
    path: '',
    loadComponent: () => import('./layout/admin-layout.component'),
    children: [
      {
        path: '',
        loadComponent: () => import('./dashboard/admin-dashboard.component')
      },
      {
        path: 'imagenes',
        loadComponent: () => import('./imagenes/imagen-biblioteca.component')
      },
      {
        path: 'destinos',
        loadComponent: () => import('./destinos/destino-list.component')
      },
      {
        path: 'paquetes',
        loadComponent: () => import('./paquetes/paquete-list.component')
      },
      {
        path: 'paquetes/nuevo',
        loadComponent: () => import('./paquetes/paquete-form.component')
      },
      {
        path: 'paquetes/edit/:id',
        loadComponent: () => import('./paquetes/paquete-form.component')
      },
      {
        path: 'agendas',
        loadComponent: () => import('./agendas/agenda-list.component')
      },
      {
        path: 'reservas',
        loadComponent: () => import('./reservas/reserva-list.component')
      },
      {
        path: 'contenidos',
        loadComponent: () => import('./contenidos/contenido-list.component')
      }
    ]
  }
] as Routes;
