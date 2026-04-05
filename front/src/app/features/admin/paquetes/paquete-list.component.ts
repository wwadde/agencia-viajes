import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminPaqueteService } from '../../../core/services/admin-paquete.service';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';

@Component({
  selector: 'app-paquete-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ImagenUrlPipe],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Paquetes Turísticos</h1>
          <p class="text-[var(--color-muted)] mt-1">Gesti&oacute;n del cat&aacute;logo de viajes y experiencias.</p>
        </div>
        <a routerLink="nuevo" class="w-full sm:w-auto px-4 py-2 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-colors flex items-center justify-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          Nuevo Paquete
        </a>
      </div>

      <!-- Error Global -->
      @if (paqueteService.error()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-[var(--color-night)]">Error</h3>
            <p class="text-sm text-[var(--color-muted)]">{{ paqueteService.error() }}</p>
          </div>
          <button (click)="paqueteService.clearError()" class="text-[var(--color-muted-soft)] hover:text-[var(--color-night)]"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
      }

      <!-- Grid view (Tablas visuales son mejores con imagenes) -->
      @if (paqueteService.loading() && paqueteService.paquetes().length === 0) {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          @for (i of [1,2,3,4]; track i) {
            <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl overflow-hidden animate-pulse">
              <div class="h-40 bg-[var(--color-sea)]/45 w-full"></div>
              <div class="p-5">
                <div class="h-5 bg-[var(--color-sea)]/45 rounded-md w-3/4 mb-3"></div>
                <div class="h-4 bg-[var(--color-sea)]/45 rounded-md w-full mb-2"></div>
                <div class="h-4 bg-[var(--color-sea)]/45 rounded-md w-2/3 mb-4"></div>
                <div class="flex justify-between items-center mt-4 pt-4 border-t border-[var(--color-sea-strong)]/35">
                  <div class="h-4 bg-[var(--color-sea)]/45 rounded-md w-1/3"></div>
                  <div class="flex gap-2">
                     <div class="h-8 w-8 bg-[var(--color-sea)]/45 rounded-md"></div>
                     <div class="h-8 w-8 bg-[var(--color-sea)]/45 rounded-md"></div>
                  </div>
                </div>
              </div>
            </div>
          }
        </div>
      } @else if (paqueteService.paquetes().length === 0) {
         <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl p-12 text-center">
            <svg class="w-16 h-16 text-[var(--color-muted-soft)] mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 class="text-lg font-medium text-[var(--color-night)] mb-2">No hay paquetes disponibles</h3>
            <p class="text-[var(--color-muted-soft)] max-w-sm mx-auto mb-6">Empieza creando el primer paquete tur&iacute;stico para mostrar en el sitio p&uacute;blico.</p>
            <a routerLink="nuevo" class="inline-flex items-center px-4 py-2 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-[var(--color-night)] font-medium rounded-xl transition-colors border border-[var(--color-sea-strong)]/45 hover:border-neutral-600">
              Crear Paquete
            </a>
         </div>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 relative">
          
          @if (paqueteService.loading()) {
            <div class="absolute inset-0 bg-white/20 backdrop-blur-[1px] z-10 flex border border-transparent rounded-2xl"></div>
          }

          @for (paq of paqueteService.paquetes(); track paq.id) {
            <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 hover:border-[var(--color-sea-strong)]/45 rounded-2xl overflow-hidden flex flex-col transition-all duration-300">
              
              <!-- Image Header -->
              <div class="relative h-48 overflow-hidden bg-[var(--color-sea)]/45">
                @if (paq.imagenPrincipal) {
                  <img [src]="paq.imagenPrincipal | imagenUrl" [alt]="paq.nombre" loading="lazy" class="w-full h-full object-cover" />
                } @else {
                  <div class="w-full h-full flex items-center justify-center text-[var(--color-muted)]">
                    <svg class="w-12 h-12 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                }

                <!-- Status Badges -->
                <div class="absolute top-3 left-3 flex flex-col gap-2">
                  <span class="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider bg-black/70 backdrop-blur-md text-white rounded-lg border border-white/10">
                    ID: {{ paq.id }}
                  </span>
                  @if (paq.destacado) {
                    <span class="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider bg-amber-500/90 backdrop-blur-md text-[var(--color-night)] rounded-lg border border-amber-300/30 flex items-center gap-1">
                      <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
                      Destacado
                    </span>
                  }
                </div>
                
                <!-- Duration Badge -->
                <div class="absolute top-3 right-3">
                  <span class="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider bg-black/70 backdrop-blur-md text-white rounded-lg border border-white/10">
                    {{ paq.duracionDias }} días
                  </span>
                </div>
              </div>

              <!-- Body -->
              <div class="p-5 flex-1 flex flex-col">
                <h3 class="text-lg font-bold text-[var(--color-night)] leading-tight mb-2 line-clamp-2" [title]="paq.nombre">{{ paq.nombre }}</h3>
                <p class="text-xs font-mono text-[var(--color-muted-soft)] mb-3 truncate">/{{ paq.slug }}</p>
                <div class="flex flex-wrap gap-2 mb-4">
                  <span class="inline-flex items-center gap-1 text-xs font-medium text-[var(--color-muted)] bg-[var(--color-sea)]/45 px-2 py-1 rounded-md border border-[var(--color-sea-strong)]/45">
                    <svg class="w-3 h-3 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                    {{ paq.destino }}
                  </span>
                </div>
                <p class="text-sm text-[var(--color-muted)] line-clamp-2 flex-1">{{ paq.descripcion }}</p>

                <!-- Footer Actions -->
                <div class="mt-5 pt-4 border-t border-[var(--color-sea-strong)]/35 flex items-center justify-between">
                  <div>
                     <a [routerLink]="['edit', paq.id]" class="text-xs font-medium text-blue-400 hover:text-blue-300 transition-colors">Editar Detalles &rarr;</a>
                  </div>
                  <div class="flex gap-2">
                    <a [routerLink]="['edit', paq.id]" class="p-2 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-[var(--color-night)] rounded-lg transition-colors border border-[var(--color-sea-strong)]/45" title="Editar">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                    </a>
                    <button (click)="deletePaquete(paq.id)" class="p-2 bg-red-900/20 hover:bg-red-500/20 text-red-500 hover:text-red-400 rounded-lg transition-colors border border-red-900/40 hover:border-red-500/40" title="Eliminar">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `
})
export default class PaqueteListComponent implements OnInit {
  paqueteService = inject(AdminPaqueteService);

  ngOnInit() {
    this.paqueteService.loadPaquetes();
  }

  async deletePaquete(id: number) {
    if (confirm('¿Est\u00e1s seguro que deseas eliminar este paquete de form permanente? Se eliminar\u00e1n las asociaciones a imágenes y salidas programadas.')) {
      await this.paqueteService.deletePaquete(id);
    }
  }
}

