import { Routes } from '@angular/router';

export default [
    {
        path: '',
        loadComponent: () => import('./mis-reservas/mis-reservas.component')
    },
    {
        path: 'resultado-pago',
        loadComponent: () => import('./resultado/resultado-pago.component')
    },
    {
        path: ':id/checkout',
        loadComponent: () => import('./checkout/checkout.component')
    },
    {
        path: ':id/resultado-pago',
        loadComponent: () => import('./resultado/resultado-pago.component')
    }
] as Routes;
