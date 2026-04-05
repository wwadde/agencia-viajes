import { Component, signal } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Dashboard Admin</h1>
      </div>

      <!-- Quick Metrics Placeholder -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        @for (item of [1,2,3,4]; track item) {
          <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl p-6 hover:border-[var(--color-sea-strong)]/45 transition-colors">
            <div class="w-10 h-10 rounded-lg bg-[var(--color-sea)]/45 flex items-center justify-center mb-4">
              <div class="h-5 w-5 bg-[var(--color-muted)]/40 rounded-full animate-pulse"></div>
            </div>
            <p class="text-sm font-medium text-[var(--color-muted)]">Métrica {{ item }}</p>
            <p class="text-3xl font-bold text-[var(--color-night)] mt-1">--</p>
          </div>
        }
      </div>
      
      <!-- Placeholder content -->
      <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl p-8 flex flex-col items-center justify-center min-h-[400px] text-center">
        <div class="w-16 h-16 rounded-full bg-blue-500/10 flex items-center justify-center mb-4">
          <svg class="w-8 h-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-[var(--color-night)] mb-2">Bienvenido al Panel de Administración</h2>
        <p class="text-[var(--color-muted)] max-w-md">
          Selecciona una opción del menú lateral para gestionar los paquetes, destinos, imágenes y usuarios de la plataforma.
        </p>
      </div>
    </div>
  `,
  host: {
    'class': 'block h-full'
  }
})
export default class AdminDashboardComponent {
}

