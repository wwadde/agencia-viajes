import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = async (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        await authService.checkAuth();
    }

    if (authService.isAuthenticated()) {
        return true;
    }

    // Require authentication, redirect to login with original URL
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const unauthGuard: CanActivateFn = async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        await authService.checkAuth();
    }

    if (!authService.isAuthenticated()) {
        return true;
    }

    const role = authService.roles();
    if (role === 'ADMIN') {
        return router.createUrlTree(['/admin']);
    }

    return router.createUrlTree(['/']);
};
