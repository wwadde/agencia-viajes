import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // 1. Siempre incluir credenciales (cookies de sesión)
    const modifiedReq = req.clone({
        withCredentials: true
    });

    return next(modifiedReq);
};
