import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-google-callback',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [RouterLink],
    template: `
    <div class="theme-shell min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div class="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 class="mt-6 text-center text-3xl font-extrabold text-[var(--color-ink)] tracking-tight">
          Autenticación con Google
        </h2>
      </div>

      <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div class="bg-white/70 overflow-hidden py-8 px-4 shadow-2xl sm:rounded-2xl sm:px-10 border border-white/80 backdrop-blur-sm text-center">
          @if (phase() === 'loading') {
            <div class="flex flex-col items-center gap-4">
              <svg class="animate-spin h-8 w-8 text-[var(--color-ink)]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" aria-hidden="true">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
              </svg>
              <p class="text-sm text-[var(--color-muted)]">Validando sesión y redirigiendo...</p>
            </div>
          } @else {
            <div class="rounded-lg bg-red-900/50 border border-red-500/50 p-4 text-left">
              <h3 class="text-sm font-medium text-red-200">No se pudo iniciar sesión con Google</h3>
              <p class="mt-2 text-xs text-red-300">{{ message() }}</p>
            </div>

            <div class="mt-6 flex flex-col gap-3">
              <button
                type="button"
                (click)="retryGoogleLogin()"
                class="w-full inline-flex items-center justify-center gap-3 py-3 px-4 rounded-xl border border-[var(--color-muted)]/35 bg-white/80 text-[var(--color-ink)] font-medium hover:bg-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-ink)] transition-all"
              >
                Reintentar con Google
              </button>

              <a [routerLink]="['/login']" [queryParams]="{ returnUrl: returnUrl() }" class="w-full inline-flex items-center justify-center py-3 px-4 rounded-xl border border-transparent shadow-lg text-sm font-medium text-white bg-[var(--color-ink)] hover:bg-[var(--color-night)] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-ink)] transition-all">
                Volver al login
              </a>
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export default class GoogleCallbackComponent implements OnInit {
    private readonly auth = inject(AuthService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    protected readonly phase = signal<'loading' | 'error'>('loading');
    protected readonly message = signal('');
    protected readonly returnUrl = signal('/');

    async ngOnInit(): Promise<void> {
        const status = this.route.snapshot.queryParamMap.get('status');
        const callbackReturnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const storedReturnUrl = this.auth.consumeGoogleReturnUrl();
        this.returnUrl.set(this.resolveReturnUrl(callbackReturnUrl ?? storedReturnUrl ?? '/'));

        if (status !== 'ok') {
            const errorMessage = this.route.snapshot.queryParamMap.get('message')
                ?? this.route.snapshot.queryParamMap.get('detail')
                ?? 'Intenta de nuevo en unos segundos.';

            this.message.set(errorMessage);
            this.phase.set('error');
            return;
        }

        await this.auth.checkAuth();

        if (!this.auth.isAuthenticated()) {
            this.message.set('La sesión no pudo validarse después del login con Google.');
            this.phase.set('error');
            return;
        }

        if (this.auth.roles() === 'ADMIN') {
            await this.router.navigateByUrl('/admin');
            return;
        }

        await this.router.navigateByUrl(this.returnUrl());
    }

    protected retryGoogleLogin(): void {
        this.auth.startGoogleLogin(this.returnUrl());
    }

    private resolveReturnUrl(url: string): string {
        if (!url.startsWith('/') || url.startsWith('//')) {
            return '/';
        }
        return url;
    }
}