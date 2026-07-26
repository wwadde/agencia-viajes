import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { UserInfoDTO, LoginDTO, RegistroUsuarioDTO } from '../models/auth.model';
import { environment } from '../../../environments/environment';
import type { ProblemDetail } from '../models/error.model';


@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly baseUrl = `${environment.apiUrl}/auth`;
    private readonly googleOAuthStartPath = '/oauth2/authorization/google';
    private readonly googleReturnUrlKey = 'auth.google.returnUrl';
    private readonly sessionHintKey = 'auth.session.active';
    private readonly authCheckCacheMs = 60_000;
    private http = inject(HttpClient);
    private authCheckInFlight: Promise<void> | null = null;
    private authStateResolved = false;
    private lastAuthCheckAt = 0;

    // Private writable state
    private _user = signal<UserInfoDTO | null>(null);
    private _loading = signal(false);
    private _error = signal<string | null>(null);
    private _violations = signal<{ campo: string; mensaje: string }[]>([]);

    // Public read-only signals
    readonly user = this._user.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();
    readonly violations = this._violations.asReadonly();

    // Derived state (computed)
    readonly isAuthenticated = computed(() => this._user() !== null);
    readonly roles = computed(() => this._user()?.rol);

    /**
     * Obtiene la información del usuario actual si existe una sesión activa
     */
    async checkAuth(force: boolean = false): Promise<void> {
        const now = Date.now();
        if (!force && this.authStateResolved && now - this.lastAuthCheckAt < this.authCheckCacheMs) {
            return;
        }

        if (this.authCheckInFlight) {
            return this.authCheckInFlight;
        }

        this.authCheckInFlight = (async () => {
            this._loading.set(true);
            this._error.set(null);
            this._violations.set([]);
            try {
                const user = await firstValueFrom(
                    this.http.get<UserInfoDTO>(`${this.baseUrl}/me`)
                );
                this._user.set(user);
                this.persistSessionHint(true);
            } catch {
                this._user.set(null); // No autenticado
                this.persistSessionHint(false);
            } finally {
                this.authStateResolved = true;
                this.lastAuthCheckAt = Date.now();
                this.authCheckInFlight = null;
                this._loading.set(false);
            }
        })();

        return this.authCheckInFlight;
    }

    /**
     * Para vistas públicas: evita consultar /auth/me si no hay indicios de sesión previa.
     */
    async checkAuthIfSessionLikelyActive(): Promise<void> {
        if (!this.isAuthenticated() && !this.hasSessionHint()) {
            return;
        }
        await this.checkAuth();
    }

    /**
     * Inicia sesión con las credenciales dadas
     */
    async login(credentials: LoginDTO): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        this._violations.set([]);
        try {
            await this.ensureCsrfToken();
            const user = await firstValueFrom(
                this.http.post<UserInfoDTO>(`${this.baseUrl}/login`, credentials)
            );
            this._user.set(user);
            this.persistSessionHint(true);
            this.authStateResolved = true;
            this.lastAuthCheckAt = Date.now();
        } catch (e) {
            if (e instanceof HttpErrorResponse) {
                // Reintento único para casos esporádicos de 403 (p.ej. cookie XSRF recién regenerada).
                if (e.status === 403) {
                    try {
                        const user = await firstValueFrom(
                            this.http.post<UserInfoDTO>(`${this.baseUrl}/login`, credentials)
                        );
                        this._user.set(user);
                        this.persistSessionHint(true);
                        this.authStateResolved = true;
                        this.lastAuthCheckAt = Date.now();
                        this._error.set(null);
                        this._violations.set([]);
                        return;
                    } catch (retryError) {
                        if (retryError instanceof HttpErrorResponse) {
                            const retryProblem = retryError.error as ProblemDetail | null;
                            this._error.set(retryProblem?.detail ?? 'Error de autenticación');
                            this._violations.set(retryProblem?.violations ?? []);
                        } else {
                            this._error.set('Error de conexión o servidor');
                            this._violations.set([]);
                        }
                        throw retryError;
                    }
                }

                const problem = e.error as ProblemDetail | null;
                this._error.set(problem?.detail ?? 'Error de autenticación');
                this._violations.set(problem?.violations ?? []);
            } else {
                this._error.set('Error de conexión o servidor');
                this._violations.set([]);
            }
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    /**
     * Registra un nuevo usuario
     */
    async register(data: RegistroUsuarioDTO): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        this._violations.set([]);
        try {
            await this.ensureCsrfToken();
            await firstValueFrom(
                this.http.post(`${this.baseUrl}/register`, data)
            );
        } catch (e) {
            if (e instanceof HttpErrorResponse) {
                const problem = e.error as ProblemDetail | null;
                this._error.set(problem?.detail ?? 'Error en el registro');
                this._violations.set(problem?.violations ?? []);
            } else {
                this._error.set('Error de conexión o servidor');
                this._violations.set([]);
            }
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    /**
     * Cierra sesión
     */
    async logout(): Promise<void> {
        this._loading.set(true);
        try {
            await this.ensureCsrfToken();
            await firstValueFrom(this.http.post(`${this.baseUrl}/logout`, {}));
        } catch (e) {
            console.error('Error al cerrar sesión', e);
        } finally {
            this._user.set(null);
            this.persistSessionHint(false);
            this.authStateResolved = true;
            this.lastAuthCheckAt = Date.now();
            this._error.set(null);
            this._loading.set(false);
        }
    }

    /**
     * Limpia el mensaje de error de forma manual
     */
    clearError(): void {
        this._error.set(null);
        this._violations.set([]);
    }

    startGoogleLogin(returnUrl?: string): void {
        this.persistGoogleReturnUrl(returnUrl);
        if (typeof window !== 'undefined') {
            window.location.assign(this.googleOAuthStartPath);
        }
    }

    consumeGoogleReturnUrl(): string | null {
        if (typeof window === 'undefined') {
            return null;
        }
        const storedUrl = window.sessionStorage.getItem(this.googleReturnUrlKey);
        window.sessionStorage.removeItem(this.googleReturnUrlKey);
        return this.isSafeReturnUrl(storedUrl) ? storedUrl : null;
    }

    async ensureCsrfToken(): Promise<void> {
        try {
            await firstValueFrom(this.http.get<void>(`${this.baseUrl}/csrf`));
        } catch {
            // Si falla, el backend seguirá validando en la mutación y devolverá el error correspondiente.
        }
    }

    private persistGoogleReturnUrl(returnUrl?: string): void {
        if (typeof window === 'undefined') {
            return;
        }

        if (this.isSafeReturnUrl(returnUrl)) {
            window.sessionStorage.setItem(this.googleReturnUrlKey, returnUrl);
            return;
        }

        window.sessionStorage.removeItem(this.googleReturnUrlKey);
    }

    private isSafeReturnUrl(url: string | null | undefined): url is string {
        return Boolean(url && url.startsWith('/') && !url.startsWith('//'));
    }

    private hasSessionHint(): boolean {
        if (typeof window === 'undefined') {
            return false;
        }
        return window.localStorage.getItem(this.sessionHintKey) === '1';
    }

    private persistSessionHint(active: boolean): void {
        if (typeof window === 'undefined') {
            return;
        }

        if (active) {
            window.localStorage.setItem(this.sessionHintKey, '1');
            return;
        }

        window.localStorage.removeItem(this.sessionHintKey);
    }
}
