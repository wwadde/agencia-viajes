import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RegistroUsuarioDTO } from '../../../core/models/auth.model';

@Component({
    selector: 'app-register',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ReactiveFormsModule, RouterLink],
    template: `
    <div class="theme-shell min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div class="sm:mx-auto sm:w-full sm:max-w-md">
        <div class="mb-4 flex justify-center">
          <a
            [routerLink]="['/']"
            class="inline-flex items-center gap-2 rounded-full border border-white/80 bg-white/65 px-4 py-2 text-sm font-medium text-[var(--color-ink)] shadow-sm hover:bg-white transition-colors focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/35"
          >
            <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M17 10a1 1 0 01-1 1H6.414l3.293 3.293a1 1 0 11-1.414 1.414l-5-5a1 1 0 010-1.414l5-5a1 1 0 111.414 1.414L6.414 9H16a1 1 0 011 1z" clip-rule="evenodd" />
            </svg>
            Volver al inicio
          </a>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-[var(--color-ink)] tracking-tight">
          Crea tu cuenta
        </h2>
        <p class="mt-2 text-center text-sm text-[var(--color-muted)]">
          O
          <a [routerLink]="['/login']" [queryParams]="{ returnUrl: route.snapshot.queryParams['returnUrl'] }" class="font-medium text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors underline underline-offset-2">
            inicia sesión si ya tienes una
          </a>
        </p>
      </div>

      <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div class="bg-white/70 overflow-hidden py-8 px-4 shadow-2xl sm:rounded-2xl sm:px-10 border border-white/80 backdrop-blur-sm">
          <form class="space-y-6" [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            
            @if (auth.error()) {
              <div class="rounded-lg bg-red-900/50 border border-red-500/50 p-4">
                <div class="flex">
                  <div class="ml-3">
                    <h3 class="text-sm font-medium text-red-200">{{ auth.error() }}</h3>
                    @if (auth.violations() && auth.violations().length > 0) {
                      <ul class="mt-2 list-disc list-inside text-xs text-red-300">
                        @for (v of auth.violations(); track v.campo) {
                          <li><strong>{{ v.campo }}</strong>: {{ v.mensaje }}</li>
                        }
                      </ul>
                    }
                  </div>
                </div>
              </div>
            }

            <div>
              <label for="usuario" class="block text-sm font-medium text-[var(--color-ink)]">
                Usuario
              </label>
              <div class="mt-2">
                <input
                  id="usuario"
                  formControlName="usuario"
                  type="text"
                  required
                  class="appearance-none block w-full px-4 py-3 bg-white/90 border border-white rounded-xl text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/30 focus:border-transparent transition-all"
                  placeholder="Tu nombre de usuario"
                />
              </div>
            </div>
            
            <div>
              <label for="correo" class="block text-sm font-medium text-[var(--color-ink)]">
                Correo Electrónico
              </label>
              <div class="mt-2">
                <input
                  id="correo"
                  formControlName="correo"
                  type="email"
                  required
                  class="appearance-none block w-full px-4 py-3 bg-white/90 border border-white rounded-xl text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/30 focus:border-transparent transition-all"
                  placeholder="correo@ejemplo.com"
                />
              </div>
            </div>

            <div>
              <label for="password" class="block text-sm font-medium text-[var(--color-ink)]">
                Contraseña
              </label>
              <div class="mt-2">
                <input
                  id="password"
                  formControlName="password"
                  type="password"
                  required
                  class="appearance-none block w-full px-4 py-3 bg-white/90 border border-white rounded-xl text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/30 focus:border-transparent transition-all"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <div>
              <button
                type="submit"
                [disabled]="registerForm.invalid || auth.loading()"
                class="w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-lg text-sm font-medium text-white bg-[var(--color-ink)] hover:bg-[var(--color-night)] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-ink)] disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                @if (auth.loading()) {
                  <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-[var(--color-night)]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Creando cuenta...
                } @else {
                  Registrarse
                }
              </button>
            </div>

            <div class="relative">
              <div class="absolute inset-0 flex items-center" aria-hidden="true">
                <div class="w-full border-t border-[var(--color-muted)]/30"></div>
              </div>
              <div class="relative flex justify-center text-xs uppercase">
                <span class="bg-white/70 px-3 text-[var(--color-muted)]">o</span>
              </div>
            </div>

            <div>
              <button
                type="button"
                (click)="continueWithGoogle()"
                [disabled]="auth.loading()"
                class="w-full inline-flex items-center justify-center gap-3 py-3 px-4 rounded-xl border border-[var(--color-muted)]/35 bg-white/80 text-[var(--color-ink)] font-medium hover:bg-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--color-ink)] disabled:opacity-50 disabled:cursor-not-allowed transition-all cursor-pointer"
              >
                <svg class="h-5 w-5" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="#4285F4" d="M23.49 12.27c0-.79-.07-1.55-.2-2.27H12v4.3h6.45a5.51 5.51 0 01-2.39 3.62v3h3.86c2.26-2.08 3.57-5.15 3.57-8.65z"/>
                  <path fill="#34A853" d="M12 24c3.24 0 5.95-1.07 7.93-2.91l-3.86-3c-1.07.72-2.43 1.15-4.07 1.15-3.13 0-5.79-2.11-6.74-4.95H1.27v3.09A12 12 0 0012 24z"/>
                  <path fill="#FBBC05" d="M5.26 14.29A7.2 7.2 0 014.88 12c0-.79.14-1.56.38-2.29V6.62H1.27A12 12 0 000 12c0 1.93.46 3.75 1.27 5.38l3.99-3.09z"/>
                  <path fill="#EA4335" d="M12 4.77c1.76 0 3.34.61 4.58 1.79l3.43-3.43A11.95 11.95 0 0012 0 12 12 0 001.27 6.62l3.99 3.09c.95-2.84 3.61-4.94 6.74-4.94z"/>
                </svg>
                Continuar con Google
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export default class RegisterComponent {
    auth = inject(AuthService);
    route = inject(ActivatedRoute);
    private fb = inject(FormBuilder);
    private router = inject(Router);

    registerForm = this.fb.group({
        usuario: ['', Validators.required],
        correo: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(4)]]
    });

    async onSubmit() {
        if (this.registerForm.valid) {
            try {
                await this.auth.register(this.registerForm.value as RegistroUsuarioDTO);
                // After successful registration, we redirect to login to sign in, preserving returnUrl
                const returnUrl = this.route.snapshot.queryParams['returnUrl'];
                this.router.navigate(['/login'], { queryParams: { returnUrl } });
            } catch (error) {
                // Error is handled in signal
            }
        }
    }

        continueWithGoogle(): void {
          const returnUrl = this.route.snapshot.queryParams['returnUrl'];
          this.auth.startGoogleLogin(returnUrl);
        }
}

