import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminReservaService } from '../../../core/services/admin-reserva.service';
import { ReservaDTO, ReservaEstado } from '../../../core/models/reserva.model';

@Component({
  selector: 'app-reserva-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Reservas</h1>
          <p class="text-[var(--color-muted)] mt-1">Gesti&oacute;n y seguimiento de las reservas de los clientes.</p>
        </div>
      </div>

      <div class="flex items-center gap-4">
        <select [(ngModel)]="estadoFiltro" (change)="loadReservas()" class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2 text-[var(--color-night)] outline-none focus:ring-2 focus:ring-[var(--color-ink)] w-full sm:w-64">
           <option [ngValue]="null">Todos los estados</option>
           <option value="PENDIENTE">Pendientes</option>
           <option value="CONFIRMADA">Confirmadas</option>
           <option value="CANCELADA">Canceladas</option>
        </select>
      </div>

      <!-- Error Global -->
      @if (reservaService.error()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-[var(--color-night)]">Error</h3>
            <p class="text-sm text-[var(--color-muted)]">{{ reservaService.error() }}</p>
          </div>
          <button (click)="reservaService.clearError()" class="text-[var(--color-muted-soft)] hover:text-[var(--color-night)]"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
      }

      <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl overflow-hidden relative">
        @if (reservaService.loading()) {
          <div class="absolute inset-0 bg-white/50 backdrop-blur-sm z-10 flex items-center justify-center">
            <div class="w-8 h-8 rounded-full border-3 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin"></div>
          </div>
        }

        <div class="overflow-x-auto">
          <table class="w-full min-w-[940px] text-left text-sm text-[var(--color-muted)]">
          <thead class="bg-[var(--color-ink)] text-xs uppercase font-medium text-[var(--color-muted-soft)] border-b border-[var(--color-sea-strong)]/35">
            <tr class="text-white">
              <th scope="col" class="px-6 py-4">ID / Fecha</th>
              <th scope="col" class="px-6 py-4">Usuario</th>
              <th scope="col" class="px-6 py-4">Paquete & Salida</th>
              <th scope="col" class="px-6 py-4">Pax / Precio</th>
              <th scope="col" class="px-6 py-4 text-center">Estado</th>
              <th scope="col" class="px-6 py-4 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-neutral-800">
            @for (reserva of reservaService.reservas(); track reserva.id) {
              <tr class="hover:bg-[var(--color-sea)]/50 transition-colors">
                <td class="px-6 py-4">
                  <div class="font-mono text-[var(--color-muted)] font-medium">#{{ reserva.id }}</div>
                  <div class="text-xs mt-1">{{ reserva.fechaReserva | date:'shortDate' }}</div>
                </td>
                <td class="px-6 py-4">
                  <div class="font-medium text-[var(--color-night)]">{{ reserva.clienteUsuario || ('Cliente ' + reserva.clienteId) }}</div>
                </td>
                <td class="px-6 py-4">
                  <div class="font-medium text-[var(--color-night)] line-clamp-1" [title]="reserva.paqueteNombre">{{ reserva.paqueteNombre }}</div>
                  <div class="text-xs mt-1">Salida: {{ reserva.fechaSalida | date:'shortDate' }}</div>
                </td>
                <td class="px-6 py-4">
                  <div class="text-[var(--color-night)]">{{ reserva.cantidadPersonas }} pers.</div>
                  <div class="font-medium mt-1">{{ reserva.precioTotal | currency:'USD' }}</div>
                </td>
                <td class="px-6 py-4 text-center">
                  <span class="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-lg border flex items-center justify-center gap-1.5 w-max mx-auto"
                        [ngClass]="{
                          'bg-yellow-50 text-yellow-700 border-yellow-300': reserva.estado === 'PENDIENTE',
                          'bg-green-50 text-green-700 border-green-300': reserva.estado === 'CONFIRMADA',
                          'bg-red-50 text-red-700 border-red-300': reserva.estado === 'CANCELADA'
                        }">
                    @if (reserva.estado === 'PENDIENTE') {
                      <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    }
                    @if (reserva.estado === 'CONFIRMADA') {
                      <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                    }
                    @if (reserva.estado === 'CANCELADA') {
                      <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                    }
                    {{ reserva.estado }}
                  </span>
                </td>
                <td class="px-6 py-4 flex items-center justify-end gap-2">
                  <button (click)="openDetail(reserva)" class="p-2 text-blue-400 hover:bg-[var(--color-night)]/10 hover:text-blue-300 rounded-lg transition-colors border border-transparent hover:border-blue-500/20" title="Ver Detalles y Cambiar Estado">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                  </button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="6" class="px-6 py-12 text-center text-[var(--color-muted-soft)]">
                  No se encontraron reservas con los filtros actuales.
                </td>
              </tr>
            }
          </tbody>
          </table>
        </div>
      </div>

      <!-- Detail Modal -->
      @if (selectedReserva()) {
        <div class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6" (click)="selectedReserva.set(null)">
          <div class="absolute inset-0 bg-black/60 backdrop-blur-sm"></div>
          
          <div class="relative bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl shadow-2xl w-full max-w-2xl overflow-hidden" (click)="$event.stopPropagation()">
            <div class="px-6 py-4 border-b border-[var(--color-sea-strong)]/35 bg-[var(--color-ink)] flex justify-between items-center">
              <h3 class="text-lg font-bold text-white">Detalle de Reserva #{{ selectedReserva()?.id }}</h3>
              <button (click)="selectedReserva.set(null)" class="text-white hover:text-[var(--color-muted)] p-1">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
              </button>
            </div>
            
            <div class="p-6 space-y-6">
               <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
                  <div>
                    <p class="text-[var(--color-muted-soft)]">Cliente ID</p>
                    <p class="font-medium text-[var(--color-night)]">{{ selectedReserva()?.clienteId }}</p>
                  </div>
                  <div>
                    <p class="text-[var(--color-muted-soft)]">Observaciones</p>
                    <p class="font-medium text-[var(--color-night)] italic">{{ selectedReserva()?.observaciones || 'Ninguna' }}</p>
                  </div>
                  <div>
                    <p class="text-[var(--color-muted-soft)]">Precio Unitario</p>
                    <p class="font-medium text-[var(--color-night)]">{{ selectedReserva()?.precioUnitario | currency:'USD' }}</p>
                  </div>
                  <div>
                    <p class="text-[var(--color-muted-soft)]">Total</p>
                    <p class="font-medium text-[var(--color-night)] text-lg">{{ selectedReserva()?.precioTotal | currency:'USD' }}</p>
                  </div>
               </div>

               <div class="border-t border-[var(--color-sea-strong)]/35 pt-6">
                 <h4 class="text-sm font-medium text-[var(--color-muted)] mb-3">Cambiar Estado</h4>
                 <div class="flex flex-col sm:flex-row gap-3">
                    <button [disabled]="selectedReserva()?.estado === 'PENDIENTE' || updatingEstado()"
                            (click)="changeEstado(selectedReserva()!.id, 'PENDIENTE')"
                            class="flex-1 py-2 text-sm font-medium rounded-xl border border-yellow-300 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
                      Marcar Pendiente
                    </button>
                    <button [disabled]="selectedReserva()?.estado === 'CONFIRMADA' || updatingEstado()"
                            (click)="changeEstado(selectedReserva()!.id, 'CONFIRMADA')"
                            class="flex-1 py-2 text-sm font-medium rounded-xl border border-green-300 bg-green-50 text-green-700 hover:bg-green-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
                      Confirmar
                    </button>
                    <button [disabled]="selectedReserva()?.estado === 'CANCELADA' || updatingEstado()"
                            (click)="changeEstado(selectedReserva()!.id, 'CANCELADA')"
                            class="flex-1 py-2 text-sm font-medium rounded-xl border border-red-300 bg-red-50 text-red-700 hover:bg-red-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
                      Cancelar
                    </button>
                 </div>
               </div>
            </div>
          </div>
        </div>
      }

    </div>
  `
})
export default class ReservaListComponent implements OnInit {
  reservaService = inject(AdminReservaService);

  estadoFiltro: ReservaEstado | null = null;
  selectedReserva = signal<ReservaDTO | null>(null);
  updatingEstado = signal(false);

  ngOnInit() {
    this.loadReservas();
  }

  loadReservas() {
    // @ts-ignore
    this.reservaService.loadReservas(this.estadoFiltro !== 'null' ? this.estadoFiltro : undefined);
  }

  openDetail(res: ReservaDTO) {
    this.selectedReserva.set(res);
  }

  async changeEstado(id: number, estado: ReservaEstado) {
    this.updatingEstado.set(true);
    try {
      const res = await this.reservaService.updateReservaEstado(id, estado);
      if (res) {
        this.selectedReserva.set(res); // update modal info
      }
    } catch (e) {
      // Error handled by service
    } finally {
      this.updatingEstado.set(false);
    }
  }
}

