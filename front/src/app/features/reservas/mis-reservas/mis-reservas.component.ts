import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ReservaDTO } from '../../../core/models/reserva.model';
import { ReservaService } from '../../../core/services/reserva.service';

@Component({
  selector: 'app-mis-reservas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="theme-shell min-h-screen text-[var(--color-ink)] flex flex-col font-sans relative">
      <!-- Navbar Simple -->
      <nav class="border-b border-white/40 bg-[var(--color-cloud)]/70 backdrop-blur-md sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex items-center h-16">
            <a routerLink="/" class="text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors flex items-center gap-2 font-medium">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
              </svg>
              Explorar Paquetes
            </a>
            <a routerLink="/" class="ml-6 text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors flex items-center gap-2 font-medium">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Inicio
            </a>
          </div>
        </div>
      </nav>

      <main class="flex-grow max-w-5xl mx-auto w-full py-12 px-4 sm:px-6 lg:px-8">
        <div class="mb-10">
          <h1 class="text-3xl font-extrabold text-[var(--color-ink)] tracking-tight">Mis Reservas</h1>
          <p class="text-[var(--color-muted)] mt-2 text-lg">Administra tus viajes y verifica el estado de tus pagos.</p>
        </div>

        @if (loading()) {
          <div class="flex items-center justify-center min-h-[40vh]">
            <div class="w-12 h-12 border-4 border-white border-t-[var(--color-ink)] rounded-full animate-spin"></div>
          </div>
        } @else if (error()) {
          <div class="bg-red-900/20 border border-red-500/30 rounded-2xl p-8 text-center">
            <svg class="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <h3 class="text-lg font-medium text-[var(--color-ink)] mb-2">Error</h3>
            <p class="text-[var(--color-muted)]">{{ error() }}</p>
          </div>
        } @else if (reservas().length === 0) {
          <div class="bg-white/70 border border-white/80 rounded-[2rem] p-16 text-center text-[var(--color-muted)] shadow-2xl">
            <svg class="w-16 h-16 text-[var(--color-muted-soft)] mx-auto mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
            <h3 class="text-2xl font-bold text-[var(--color-ink)] mb-2">No tienes reservas</h3>
            <p class="mb-8">Explora nuestros paquetes y programa tu próximo viaje.</p>
            <a routerLink="/" class="inline-flex items-center justify-center px-8 py-3.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors shadow-lg">
              Explorar paquetes
            </a>
          </div>
        } @else {
          <div class="space-y-6">
            @for (reserva of reservas(); track reserva.id) {
              <div class="bg-white/70 border border-white/80 rounded-3xl overflow-hidden hover:border-[var(--color-sea-strong)] transition-colors shadow-xl">
                <div class="p-6 sm:p-8 flex flex-col lg:flex-row gap-8 items-start lg:items-center justify-between">
                  
                  <!-- Info Basica -->
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-3">
                      <span class="px-3 py-1 text-xs font-bold uppercase tracking-wider rounded-lg border bg-white"
                            [ngClass]="{
                              'text-yellow-500 border-yellow-500/20': reserva.estado === 'PENDIENTE',
                              'text-green-500 border-green-500/20': reserva.estado === 'CONFIRMADA',
                              'text-red-500 border-red-500/20': reserva.estado === 'CANCELADA'
                            }">
                        {{ reserva.estado }}
                      </span>
                      <span class="text-sm font-mono text-[var(--color-muted-soft)]">RES-{{ reserva.id }}</span>
                      <span class="text-sm text-[var(--color-muted-soft)]">{{ reserva.fechaReserva | date:'shortDate' }}</span>
                    </div>
                    <h3 class="text-2xl font-bold text-[var(--color-night)] mb-2">{{ reserva.paqueteNombre }}</h3>
                    <div class="flex flex-wrap items-center gap-4 sm:gap-6 text-sm text-[var(--color-muted)]">
                      <div class="flex items-center gap-1.5">
                        <svg class="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                        Salida: <strong class="text-[var(--color-night)]">{{ reserva.fechaSalida | date:'mediumDate' }}</strong>
                      </div>
                      <div class="flex items-center gap-1.5">
                        <svg class="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
                        <strong class="text-[var(--color-night)]">{{ reserva.cantidadPersonas }}</strong> Pasajero(s)
                      </div>
                    </div>
                  </div>

                  <!-- Pago Area -->
                  <div class="w-full lg:w-auto p-5 bg-white/90 rounded-2xl border border-white flex flex-col sm:flex-row lg:flex-col items-center justify-between gap-5">
                    <div class="text-center sm:text-left lg:text-center w-full">
                      <p class="text-xs text-[var(--color-muted-soft)] uppercase tracking-widest font-semibold mb-1">Total a pagar</p>
                      <p class="text-3xl font-bold text-[var(--color-night)]">{{ reserva.precioTotal | currency:'USD' }}</p>
                    </div>
                    
                    @if (reserva.estado === 'PENDIENTE') {
                      <div class="flex flex-col sm:flex-row gap-3 w-full lg:w-auto">
                        <button (click)="preguntarCancelar(reserva.id)" [disabled]="actionLoading() === reserva.id" class="px-5 py-3 border border-red-500/30 hover:bg-red-500/10 text-red-500 font-medium rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center">
                          @if (actionLoading() === reserva.id) {
                            <div class="w-5 h-5 border-2 border-red-500 border-t-transparent rounded-full animate-spin"></div>
                          } @else {
                            Cancelar
                          }
                        </button>
                        <a [routerLink]="['/mis-reservas', reserva.id, 'checkout']" class="w-full sm:w-auto px-8 py-3 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors shadow-lg flex justify-center items-center gap-2 whitespace-nowrap">
                          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
                          Pagar con PayU
                        </a>
                      </div>
                    } @else if (reserva.estado === 'CONFIRMADA') {
                       <div class="flex flex-col items-center gap-2 w-full lg:w-auto">
                         <div class="w-full sm:w-auto px-6 py-3 bg-green-500/10 text-green-500 font-medium rounded-xl border border-green-500/20 flex justify-center items-center gap-2">
                           <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                           Pago Aprobado
                         </div>
                         <a [routerLink]="['/mis-reservas', reserva.id, 'resultado-pago']" class="text-xs text-[var(--color-ink)] hover:text-[var(--color-night)] font-medium transition-colors underline underline-offset-2">Ver historial de pago</a>
                       </div>
                    } @else {
                       <div class="flex flex-col items-center gap-2 w-full lg:w-auto">
                         <div class="w-full sm:w-auto px-6 py-3 bg-red-500/10 text-red-500 font-medium rounded-xl border border-red-500/20 text-center">
                          Reserva Cancelada
                         </div>
                         <a [routerLink]="['/mis-reservas', reserva.id, 'resultado-pago']" class="text-xs text-[var(--color-ink)] hover:text-[var(--color-night)] font-medium transition-colors underline underline-offset-2">Ver historial de pago</a>
                       </div>
                    }
                  </div>

                </div>
              </div>
            }
          </div>
        }
      </main>

      <!-- Custom Cancel Modal -->
      @if (cancelModalOpen()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/70 backdrop-blur-sm" (click)="cancelModalOpen.set(false)"></div>
          <div class="relative bg-white border border-white rounded-3xl p-8 max-w-sm w-full shadow-2xl text-center transform transition-all duration-300 scale-100">
            <div class="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-5 border-2 border-red-500/20 text-red-500">
              <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <h3 class="text-xl font-bold text-[var(--color-ink)] mb-2">Anular Reserva</h3>
            <p class="text-[var(--color-muted)] text-sm mb-8">¿Estás completamente seguro de querer cancelar este viaje? Los cupos se liberarán inmediatamente.</p>
            <div class="flex flex-col sm:flex-row gap-3">
              <button (click)="cancelModalOpen.set(false)" class="flex-1 px-4 py-3 bg-[var(--color-sea)] hover:bg-[var(--color-sea-strong)] text-[var(--color-ink)] font-medium rounded-xl transition-colors">
                Mantener
              </button>
              <button (click)="ejecutarCancelacion()" class="flex-1 px-4 py-3 bg-red-600 hover:bg-red-500 text-white font-medium rounded-xl transition-colors shadow-lg shadow-red-500/20">
                Sí, Anular
              </button>
            </div>
          </div>
        </div>
      }

      <!-- Custom Error Toast -->
      @if (actionError()) {
        <div class="fixed bottom-6 right-6 z-50 bg-red-900/90 border border-red-500 rounded-2xl p-4 shadow-2xl shadow-red-900/50 flex items-start gap-3 max-w-sm overflow-hidden animate-in slide-in-from-bottom-5 duration-300">
          <svg class="w-6 h-6 text-red-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="flex-1">
            <h4 class="text-sm font-bold text-[var(--color-night)]">No se pudo procesar</h4>
            <p class="text-xs text-red-200 mt-0.5">{{ actionError() }}</p>
          </div>
          <button (click)="actionError.set(null)" class="text-red-400 hover:text-[var(--color-night)]">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
      }
    </div>
  `
})
export default class MisReservasComponent implements OnInit {
  private http = inject(HttpClient);
  private reservaService = inject(ReservaService);
  private baseUrl = `${environment.apiUrl}/reservas`;

  reservas = signal<ReservaDTO[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  actionLoading = signal<number | null>(null);
  actionError = signal<string | null>(null);
  cancelModalOpen = signal(false);
  cancelModalReservaId = signal<number | null>(null);

  async ngOnInit() {
    await this.loadReservas();
  }

  async loadReservas() {
    this.loading.set(true);
    try {
      const data = await firstValueFrom(this.http.get<ReservaDTO[]>(this.baseUrl));
      // Sort by creation date DESC
      const sorted = (data || []).sort((a, b) => new Date(b.fechaReserva).getTime() - new Date(a.fechaReserva).getTime());
      this.reservas.set(sorted);
    } catch (e) {
      this.error.set('No se pudieron cargar tus reservas.');
    } finally {
      this.loading.set(false);
    }
  }

  preguntarCancelar(id: number) {
    this.cancelModalReservaId.set(id);
    this.cancelModalOpen.set(true);
    this.actionError.set(null);
  }

  async ejecutarCancelacion() {
    const id = this.cancelModalReservaId();
    if (!id) return;

    this.cancelModalOpen.set(false);
    this.actionLoading.set(id);
    this.actionError.set(null);
    
    const success = await this.reservaService.cancelarReserva(id);
    if (success) {
      await this.loadReservas(); // Recargar tras cancelación exitosa
    } else {
      this.actionError.set(this.reservaService.error() || 'Ocurrió un error al intentar cancelar.');
      // Auto dismiss after 5s
      setTimeout(() => this.actionError.set(null), 5000);
    }
    this.actionLoading.set(null);
  }
}

