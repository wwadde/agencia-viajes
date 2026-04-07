import { Routes } from '@angular/router';
import { authGuard, unauthGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./features/home/home.component')
    },
    {
        path: 'paquetes/:slug',
        loadComponent: () => import('./features/paquetes/paquete-detalle.component')
    },
    {
        path: 'foro',
        loadComponent: () => import('./features/foro/foro-list.component')
    },
    {
        path: 'foro/:slug',
        loadComponent: () => import('./features/foro/foro-detalle.component')
    },
    {
        path: 'login',
        canActivate: [unauthGuard],
        loadComponent: () => import('./features/auth/login/login.component')
    },
    {
        path: 'register',
        canActivate: [unauthGuard],
        loadComponent: () => import('./features/auth/register/register.component')
    },
    {
        path: 'auth/google/callback',
        loadComponent: () => import('./features/auth/google-callback/google-callback.component')
    },
    {
        path: 'mis-reservas',
        canActivate: [authGuard],
        loadChildren: () => import('./features/reservas/reservas.routes')
    },
    {
        path: 'admin',
        canActivate: [adminGuard],
        loadChildren: () => import('./features/admin/admin.routes')
    },
    {
        path: '**',
        loadComponent: () => import('./features/not-found/not-found.component')
    }
];
