import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PaqueteService } from '../../core/services/paquete.service';
import { AuthService } from '../../core/services/auth.service';
import { SeoService } from '../../core/services/seo.service';
import { ReservaService } from '../../core/services/reserva.service';
import { PaqueteDetalleDTO, SalidaDTO } from '../../core/models/paquete.model';
import { ImagenUrlPipe } from '../../core/pipes/imagen-url.pipe';
import { ImagenMobileSrcsetPipe } from '../../core/pipes/imagen-mobile-srcset.pipe';

@Component({
  selector: 'app-paquete-detalle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FormsModule, ImagenUrlPipe, ImagenMobileSrcsetPipe, NgOptimizedImage],
  template: `
    <div class="theme-shell min-h-screen text-[var(--color-ink)] flex flex-col">
      <!-- Navbar Simple -->
      <nav class="border-b border-white/40 bg-[var(--color-cloud)]/70 backdrop-blur-md sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex items-center h-16">
            <a routerLink="/" class="text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors flex items-center gap-2 font-medium">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Volver a paquetes
            </a>
          </div>
        </div>
      </nav>
      
      <main class="flex-grow max-w-7xl mx-auto w-full py-12 px-4 sm:px-6 lg:px-8">
        @if (loading()) {
          <div class="flex items-center justify-center min-h-[50vh]">
            <div class="w-12 h-12 border-4 border-[var(--color-sea-strong)]/35 border-t-[var(--color-ink)] rounded-full animate-spin"></div>
          </div>
        } @else if (paquete()) {
          @let p = paquete()!;
          <div class="space-y-12">
            
            <!-- Hero Image and Basic Info -->
            <div class="relative h-[60vh] sm:h-[70vh] rounded-[2.5rem] overflow-hidden shadow-2xl border border-white/50">
              @if (p.imagenes && p.imagenes.length > 0) {
                <picture>
                  <source
                    media="(max-width: 767px)"
                    [attr.srcset]="p.imagenes[0].url | imagenMobileSrcset"
                    sizes="100vw"
                    type="image/webp"
                  />
                  <img
                    [ngSrc]="p.imagenes[0].url | imagenUrl"
                    [alt]="p.nombre"
                    fill
                    sizes="100vw"
                    class="w-full h-full object-cover"
                  />
                </picture>
              } @else {
                <div class="w-full h-full bg-white/90 flex items-center justify-center text-[var(--color-muted)]">
                  <svg class="w-24 h-24 opacity-20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              }
              
              <div class="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent"></div>
              
              <div class="absolute bottom-0 left-0 right-0 p-8 sm:p-12">
                <div class="flex flex-wrap items-center gap-3 mb-4">
                  <span class="px-4 py-1.5 text-xs font-bold uppercase tracking-widest bg-[var(--color-ink)] text-white rounded-full">
                    {{ p.destino.nombre }}
                  </span>
                  <span class="px-4 py-1.5 text-xs font-bold uppercase tracking-widest bg-white/10 backdrop-blur-lg text-white rounded-full border border-white/20">
                    {{ p.duracionDias }} días
                  </span>
                </div>
                <h1 class="text-4xl sm:text-6xl lg:text-7xl font-extrabold text-white tracking-tight leading-tight">
                  {{ p.nombre }}
                </h1>
              </div>
            </div>
            
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-12">
              
              <!-- Left Column: Details & Gallery -->
              <div class="col-span-1 lg:col-span-2 space-y-12">
                 
                 <!-- Description -->
                 <section class="prose prose-lg max-w-none prose-p:text-[var(--color-muted)] prose-headings:text-[var(--color-ink)]">
                   <h2 class="text-3xl font-bold tracking-tight mb-6">Acerca de este viaje</h2>
                   <p class="whitespace-pre-wrap text-lg leading-relaxed">{{ p.descripcion }}</p>
                 </section>

                 <!-- Full Detail -->
                 @if (p.detalle) {
                   <section class="bg-white/65 p-8 sm:p-10 rounded-3xl border border-white/80">
                     <h3 class="text-2xl font-bold text-[var(--color-ink)] tracking-tight mb-6">Detalles del itinerario</h3>
                     <div class="text-[var(--color-muted)] whitespace-pre-wrap leading-relaxed">{{ p.detalle }}</div>
                   </section>
                 }
                 
                 <!-- Gallery -->
                 @if(p.imagenes && p.imagenes.length > 1) {
                   <section>
                      <h3 class="text-2xl font-bold text-[var(--color-ink)] tracking-tight mb-6">Galería</h3>
                      <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
                        @for(img of p.imagenes; track img.id) {
                          <div class="relative group aspect-square rounded-2xl overflow-hidden bg-white/65 border border-white/80">
                            <picture>
                              <source
                                media="(max-width: 767px)"
                                [attr.srcset]="img.url | imagenMobileSrcset"
                                sizes="50vw"
                                type="image/webp"
                              />
                              <img
                                [ngSrc]="img.url | imagenUrl"
                                [alt]="'Imagen de ' + p.nombre"
                                fill
                                loading="lazy"
                                sizes="(max-width: 639px) 50vw, 33vw"
                                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                              />
                            </picture>
                          </div>
                        }
                      </div>
                   </section>
                 }
              </div>
              
              <!-- Right Column: Salidas & Booking -->
              <div class="col-span-1">
                 <div class="bg-white/70 p-8 rounded-3xl border border-white/80 sticky top-24 shadow-2xl backdrop-blur-sm">
                   <h3 class="text-xl font-bold text-[var(--color-ink)] tracking-tight mb-2">Salidas Programadas</h3>
                   <p class="text-sm text-[var(--color-muted)] mb-8">Selecciona la fecha para este paquete</p>
                   
                   @if (p.salidas && p.salidas.length > 0) {
                     <div class="space-y-4">
                       @for(salida of p.salidas; track salida.id) {
                         <div class="bg-white/90 p-5 rounded-2xl border border-white hover:border-[var(--color-sea-strong)] transition-all group">
                           <div class="flex justify-between items-start mb-3">
                             <div>
                               <div class="text-[var(--color-ink)] font-semibold text-lg">{{ salida.fechaSalida | date:'dd MMM yyyy' }}</div>
                               <div class="text-xs text-green-400 font-medium mt-1">
                                 @if (salida.cuposDisponibles > 0) {
                                   {{ salida.cuposDisponibles }} cupos disponibles
                                 } @else {
                                   <span class="text-red-400">Agotado</span>
                                 }
                               </div>
                             </div>
                             <div class="text-right">
                               <div class="text-[var(--color-muted)] text-xs uppercase font-bold tracking-wider mb-1">Precio</div>
                               <div class="text-[var(--color-ink)] font-bold text-xl">{{ salida.precio | currency:'USD' }}</div>
                             </div>
                           </div>
                           <button (click)="abrirModalReserva(salida)" [disabled]="salida.cuposDisponibles <= 0" class="w-full mt-2 py-3 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:bg-[var(--color-muted)] disabled:text-white/70 disabled:cursor-not-allowed text-white font-medium rounded-xl transition-colors shadow-lg disabled:shadow-none focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-white focus:ring-[var(--color-ink)]">
                             {{ salida.cuposDisponibles > 0 ? 'Reservar Lugar' : 'Agotado' }}
                           </button>
                         </div>
                       }
                     </div>
                   } @else {
                     <div class="text-center py-8">
                       <svg class="w-12 h-12 text-[var(--color-muted)]/70 mx-auto mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                         <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                       </svg>
                       <p class="text-[var(--color-muted)] font-medium">No hay salidas programadas</p>
                       <p class="text-[var(--color-muted)] text-sm mt-1">Vuelve a revisar pronto.</p>
                     </div>
                   }
                 </div>
              </div>
              
            </div>
          </div>
        } @else {
          <!-- Error Component -->
          <div class="text-center py-20 bg-white/70 border border-white/80 rounded-3xl max-w-2xl mx-auto mt-10 shadow-2xl">
            <svg class="w-16 h-16 text-[var(--color-muted)]/70 mx-auto mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h2 class="text-3xl font-bold text-[var(--color-ink)] mb-3">Paquete no encontrado</h2>
            <p class="text-[var(--color-muted)] mb-8 max-w-md mx-auto">No se pudo encontrar el paquete turístico solicitado, o el enlace no es válido.</p>
            <a routerLink="/" class="inline-flex items-center justify-center px-8 py-3 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors">
              Explorar otros paquetes
            </a>
          </div>
        }
        
        <!-- Reserva Modal -->
        @if (reservaModalOpen()) {
          <div class="fixed inset-0 bg-black/60 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
            <div class="bg-white/90 border border-white rounded-[2rem] w-full max-w-lg shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
              <div class="p-6 sm:p-8">
                <div class="flex justify-between items-start mb-6">
                  <div>
                    <h3 class="text-2xl font-bold text-[var(--color-ink)]">Confirmar Reserva</h3>
                    <p class="text-[var(--color-muted)] text-sm mt-1">Ingresa los detalles finales de tu viaje.</p>
                  </div>
                  <button (click)="cerrarModalReserva()" [disabled]="reservaService.loading() || reservaRedirecting()" class="p-2 text-[var(--color-muted)] hover:text-[var(--color-ink)] hover:bg-[var(--color-sea)]/60 rounded-full transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                  </button>
                </div>
                
                @if (salidaSeleccionada(); as salida) {
                  <div class="bg-[var(--color-cloud)] p-4 rounded-xl border border-white mb-6">
                    <div class="flex justify-between text-sm mb-2">
                       <span class="text-[var(--color-muted)]">Fecha de salida:</span>
                       <span class="text-[var(--color-ink)] font-medium">{{ salida.fechaSalida | date:'mediumDate' }}</span>
                    </div>
                    <div class="flex justify-between text-sm mb-2">
                       <span class="text-[var(--color-muted)]">Precio por persona:</span>
                       <span class="text-[var(--color-ink)] font-medium">{{ salida.precio | currency:'USD' }}</span>
                    </div>
                    <div class="flex justify-between text-sm">
                       <span class="text-[var(--color-muted)]">Cupos disponibles:</span>
                       <span class="text-green-400 font-medium">{{ salida.cuposDisponibles }}</span>
                    </div>
                  </div>
                }

                @if (reservaService.error()) {
                  <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 mb-6">
                    <p class="text-red-400 text-sm font-medium">{{ reservaService.error() }}</p>
                  </div>
                }

                <form (ngSubmit)="confirmarReserva()" #formDir="ngForm" class="space-y-5">
                  <div>
                    <label class="block text-sm font-medium text-[var(--color-ink)] mb-1.5">Cantidad de Personas</label>
                    <input type="number" [(ngModel)]="reservaForm.cantidadPersonas" name="cantidad" required min="1" [max]="salidaSeleccionada()?.cuposDisponibles || 1"
                           class="w-full px-4 py-3 bg-white border border-white rounded-xl text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/30 focus:border-[var(--color-ink)] transition-all">
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-[var(--color-ink)] mb-1.5">Observaciones (Opcional)</label>
                    <textarea [(ngModel)]="reservaForm.observaciones" name="observaciones" rows="3" placeholder="Restricciones alimentarias, requerimientos especiales..."
                              class="w-full px-4 py-3 bg-white border border-white rounded-xl text-[var(--color-ink)] placeholder-[var(--color-muted)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/30 focus:border-[var(--color-ink)] transition-all"></textarea>
                  </div>
                  
                  <div class="pt-4 flex gap-3">
                    <button type="button" (click)="cerrarModalReserva()" [disabled]="reservaService.loading() || reservaRedirecting()" class="flex-1 py-3 px-4 border border-[var(--color-muted)]/30 hover:bg-[var(--color-sea)]/45 text-[var(--color-ink)] font-medium rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                      Cancelar
                    </button>
                    <button type="submit" [disabled]="formDir.invalid || reservaService.loading() || reservaRedirecting()" 
                            class="flex-1 py-3 px-4 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:bg-[var(--color-muted)] disabled:text-white/70 disabled:cursor-not-allowed text-white font-medium rounded-xl transition-colors shadow-lg focus:outline-none">
                      {{ reservaService.loading() ? 'Procesando...' : (reservaRedirecting() ? 'Redirigiendo...' : 'Confirmar y Pagar') }}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        }
      </main>
      
      <!-- Footer Simple -->
      <footer class="border-t border-white/40 bg-[var(--color-cloud)] py-8 mt-auto">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-[var(--color-muted)] text-sm">
          <p>&copy; 2026 Agencia Viajes.</p>
        </div>
      </footer>
    </div>
  `
})
export default class PaqueteDetalleComponent implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  paqueteService = inject(PaqueteService);
  auth = inject(AuthService);
  seo = inject(SeoService);
  reservaService = inject(ReservaService);

  paquete = signal<PaqueteDetalleDTO | null>(null);
  loading = signal(true);

  reservaModalOpen = signal(false);
  salidaSeleccionada = signal<SalidaDTO | null>(null);
  reservaRedirecting = signal(false);

  reservaForm = {
    cantidadPersonas: 1,
    observaciones: ''
  };

  async ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      const result = await this.paqueteService.getPaqueteDetalle(slug);
      this.paquete.set(result);
      if (result) {
        this.seo.setPackageSeo(result, slug);
      } else {
        this.seo.setPackageNotFoundSeo();
      }
    }
    this.loading.set(false);
  }

  abrirModalReserva(salida: SalidaDTO) {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    this.salidaSeleccionada.set(salida);
    this.reservaForm = { cantidadPersonas: 1, observaciones: '' };
    this.reservaModalOpen.set(true);
  }

  cerrarModalReserva() {
    this.reservaModalOpen.set(false);
    this.salidaSeleccionada.set(null);
    this.reservaService.clearError();
  }

  async confirmarReserva() {
    if (this.reservaService.loading() || this.reservaRedirecting()) return;

    const salida = this.salidaSeleccionada();
    if (!salida) return;

    const dto = {
      salidaId: salida.id,
      cantidadPersonas: this.reservaForm.cantidadPersonas,
      observaciones: this.reservaForm.observaciones
    };

    const reservaCreada = await this.reservaService.crearReserva(dto);
    if (reservaCreada) {
      this.reservaRedirecting.set(true);
      this.reservaModalOpen.set(false);
      this.salidaSeleccionada.set(null);
      this.reservaService.clearError();
      await this.router.navigate(['/mis-reservas']);
    }
  }
}

