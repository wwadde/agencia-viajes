import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = async () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        await authService.checkAuth();
    }

    if (authService.isAuthenticated() && authService.roles() === 'ADMIN') {
        return true;
    }

    // Require authentication and ADMIN role, redirect to home or login
    if (authService.isAuthenticated()) {
        return router.createUrlTree(['/']);
    } else {
        return router.createUrlTree(['/login']);
    }
};
