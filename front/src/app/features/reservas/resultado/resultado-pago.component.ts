import { ChangeDetectionStrategy, Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PagoService } from '../../../core/services/pago.service';
import { PagoEstado, PagoResumenDTO } from '../../../core/models/pago.model';

@Component({
  selector: 'app-resultado-pago',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="theme-shell min-h-screen flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8 font-sans">
      <div class="max-w-xl w-full bg-white/70 p-8 sm:p-10 rounded-[2.5rem] shadow-2xl border border-white/80 text-center relative overflow-hidden">
        
        <!-- Estado: Evaluando Historial o Null -->
        @if (estadoFinal() === 'POLLING' || estadoFinal() === 'UNKNOWN') {
           <div class="absolute -top-24 -right-24 w-48 h-48 bg-[var(--color-sea)]/60 rounded-full blur-3xl animate-pulse"></div>
           <div class="relative z-10">
             <div class="w-20 h-20 border-4 border-white border-t-[var(--color-ink)] rounded-full animate-spin mx-auto mb-8"></div>
             <h2 class="text-2xl font-bold text-[var(--color-ink)] mb-2">Verificando Pago</h2>
             <p class="text-[var(--color-muted)] text-sm">
                Sincronizando la transacción con tu entidad bancaria. Por favor, espera...
             </p>
           </div>
        }
        
        <!-- Estado: Sin pagos intentados o empty array -->
        @if (estadoFinal() === 'NO_PAGOS') {
           <div class="absolute -top-24 -left-24 w-48 h-48 bg-[var(--color-muted)]/10 rounded-full blur-3xl"></div>
           <div class="relative z-10">
             <div class="w-20 h-20 bg-[var(--color-sea)]/45 border-2 border-[var(--color-sea-strong)]/45 text-[var(--color-muted-soft)] rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
             </div>
             <h2 class="text-3xl font-extrabold text-[var(--color-ink)] tracking-tight mb-2">Sin Historial</h2>
             <p class="text-[var(--color-muted)] text-sm mb-8">
                No hemos detectado ningún intento de pago para la reserva <strong>#{{ reservaId() }}</strong> todavía.
             </p>
             <div class="space-y-4">
               <a [routerLink]="['/mis-reservas', reservaId(), 'checkout']" class="block w-full py-3.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors shadow-lg">
                 Ir a pagar ahora
               </a>
               <a routerLink="/mis-reservas" class="block w-full py-3 border border-[var(--color-muted)]/40 hover:border-[var(--color-muted)] text-[var(--color-muted)] hover:text-[var(--color-ink)] font-medium rounded-xl transition-colors">
                 Volver a la lista
               </a>
             </div>
           </div>
        }

        <!-- Estado APROBADO -->
        @if (estadoFinal() === 'APROBADO') {
           <div class="absolute -top-24 -left-24 w-48 h-48 bg-green-500/20 rounded-full blur-3xl"></div>
           <div class="relative z-10">
             <div class="w-20 h-20 bg-green-500/10 border-2 border-green-500/30 text-green-500 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
             </div>
             <h2 class="text-3xl font-extrabold text-green-400 tracking-tight mb-2">¡Pago Exitoso!</h2>
             <p class="text-[var(--color-muted)] text-sm mb-8">
                Tu viaje ha sido garantizado. Acabamos de confirmar el pago de la reserva <strong>#{{ reservaId() }}</strong>.
             </p>
           </div>
        }

        <!-- Estado RECHAZADO / CANCELADO / EXPIRADO -->
        @if (estadoFinal() === 'RECHAZADO' || estadoFinal() === 'CANCELADO' || estadoFinal() === 'EXPIRADO') {
           <div class="absolute -bottom-24 -right-24 w-48 h-48 bg-red-500/20 rounded-full blur-3xl"></div>
           <div class="relative z-10">
             <div class="w-20 h-20 bg-red-500/10 border-2 border-red-500/30 text-red-500 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
             </div>
             <h2 class="text-3xl font-extrabold text-red-500 tracking-tight mb-2">Pago {{ estadoFinal() | lowercase | titlecase }}</h2>
             <p class="text-[var(--color-muted)] text-sm mb-8">
                El último intento de pago no fue exitoso. Verifica tus fondos o intenta con otra tarjeta de crédito/débito.
             </p>
             <div class="space-y-3 mb-8">
               <a [routerLink]="['/mis-reservas', reservaId(), 'checkout']" class="block w-full py-3.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors shadow-lg">
                 Reintentar Pago
               </a>
             </div>
           </div>
        }

        <!-- Timeout (Still pending) State -->
        @if (estadoFinal() === 'TIMEOUT' || estadoFinal() === 'PENDIENTE' || estadoFinal() === 'REQUIERE_ACCION') {
           <div class="absolute -top-24 -right-24 w-48 h-48 bg-yellow-500/10 rounded-full blur-3xl"></div>
           <div class="relative z-10">
             <div class="w-20 h-20 bg-yellow-500/10 border-2 border-yellow-500/30 text-yellow-500 rounded-full flex items-center justify-center mx-auto mb-6">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
             </div>
             <h2 class="text-3xl font-extrabold text-yellow-500 tracking-tight mb-2">Pago en Validación</h2>
             <p class="text-[var(--color-muted)] text-sm mb-8">
                Tu banco, o la red en efectivo (Baloto/Efecty), puede tardar en confirmarnos el pago. Se reflejará en esta lista cuando se apruebe.
             </p>
           </div>
        }

        <!-- HISTORIAL LIST (Mostrar si hay pagos) -->
        @if (estadoFinal() !== 'POLLING' && estadoFinal() !== 'UNKNOWN' && estadoFinal() !== 'NO_PAGOS' && historialPagos().length > 0) {
           <div class="relative z-10 border-t border-white/80 pt-6 mt-4">
             <h3 class="text-left text-sm font-bold text-[var(--color-ink)] uppercase tracking-widest mb-4">Registro de Transacciones</h3>
             <div class="space-y-3 max-h-60 overflow-y-auto pr-2 custom-scrollbar">
               @for (pago of historialPagos(); track pago.id) {
                 <div class="bg-white/90 border border-white rounded-xl p-4 flex justify-between items-center text-left hover:border-[var(--color-sea)] transition-colors">
                   <div>
                     <p class="text-[var(--color-ink)] font-medium flex items-center gap-2">
                       {{ pago.monto | currency:pago.moneda }}
                       <span class="text-[10px] px-2 py-0.5 rounded border font-bold tracking-wider"
                             [ngClass]="{
                               'bg-green-500/10 text-green-500 border-green-500/20': pago.estado === 'APROBADO',
                               'bg-red-500/10 text-red-500 border-red-500/20': pago.estado === 'RECHAZADO' || pago.estado === 'CANCELADO' || pago.estado === 'EXPIRADO',
                               'bg-yellow-500/10 text-yellow-500 border-yellow-500/20': pago.estado === 'PENDIENTE' || pago.estado === 'REQUIERE_ACCION'
                             }">
                         {{ pago.estado }}
                       </span>
                     </p>
                     <p class="text-xs text-[var(--color-muted)] mt-1">{{ pago.creadoEn | date:'medium' }} • {{ pago.proveedor }}</p>
                   </div>
                   <div class="text-xs font-mono text-[var(--color-muted)] bg-[var(--color-sea)]/45 px-2 py-1 rounded">Ref. #{{ pago.id }}</div>
                 </div>
               }
             </div>
             <div class="mt-8">
               <a routerLink="/mis-reservas" class="block w-full py-3 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors">
                 Regresar a mis reservas
               </a>
             </div>
           </div>
        }
      </div>
    </div>
  `
})
export default class ResultadoPagoComponent implements OnInit, OnDestroy {
  route = inject(ActivatedRoute);
  pagoService = inject(PagoService);

  reservaId = signal<number | null>(null);
  estadoFinal = signal<'POLLING' | 'UNKNOWN' | 'TIMEOUT' | 'NO_PAGOS' | PagoEstado>('POLLING');
  historialPagos = signal<PagoResumenDTO[]>([]);
  
  private pollingInterval: number | null = null;
  private maxAttempts = 15; // 3 secs * 15 = 45 secs max polling if pending currently
  private currentAttempt = 0;

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id') ?? this.route.snapshot.queryParamMap.get('reservaId');
    if (idParam) {
      const reservaId = Number(idParam);
      if (Number.isNaN(reservaId) || reservaId <= 0) {
        this.estadoFinal.set('UNKNOWN');
        return;
      }
      this.reservaId.set(reservaId);
      this.iniciarFlujo(reservaId);
    } else {
      this.estadoFinal.set('UNKNOWN');
    }
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  private async iniciarFlujo(rId: number) {
    // 1. Fetch initial status immediately to know if we even need to poll
    const historial = await this.pagoService.obtenerPagosReserva(rId);
    
    if (!historial || historial.length === 0) {
      // User hasn't even created a payment intent
      this.estadoFinal.set('NO_PAGOS');
      return;
    }

    // Sort by creation date DESC
    const attemptList = historial.sort((a, b) => new Date(b.creadoEn).getTime() - new Date(a.creadoEn).getTime());
    this.historialPagos.set(attemptList);
    
    const ultimo = attemptList[0];

    // 2. If the newest payment is already resolved in a terminal state, just show it. NO POLLING REQUIRED.
    if (this.isTerminalState(ultimo.estado)) {
       this.estadoFinal.set(ultimo.estado);
       return;
    }

    // 3. Otherwise, it's PENDIENTE / REQUIERE_ACCION. Show the view as POLLING and start checking in the background.
    this.estadoFinal.set('POLLING');
    this.startPolling(rId);
  }

  private startPolling(rId: number) {
    this.pollingInterval = window.setInterval(async () => {
        this.currentAttempt++;
        
        if (this.currentAttempt >= this.maxAttempts) {
            this.stopPolling();
            // If it times out but the list tells us it's PENDIENTE, leave it as PENDIENTE
            const currentList = this.historialPagos();
            if (currentList.length > 0) {
               this.estadoFinal.set(currentList[0].estado);
            } else {
               this.estadoFinal.set('TIMEOUT');
            }
            return;
        }

        const historial = await this.pagoService.obtenerPagosReserva(rId);
        if (!historial || historial.length === 0) return;

        const attemptList = historial.sort((a, b) => new Date(b.creadoEn).getTime() - new Date(a.creadoEn).getTime());
        this.historialPagos.set(attemptList);
        const ultimo = attemptList[0];

        if (this.isTerminalState(ultimo.estado)) {
           this.stopPolling();
           this.estadoFinal.set(ultimo.estado);
        }
    }, 3000);
  }

  private stopPolling() {
      if (this.pollingInterval !== null) {
          window.clearInterval(this.pollingInterval);
          this.pollingInterval = null;
      }
  }
  
  private isTerminalState(estado: string): boolean {
      return ['APROBADO', 'RECHAZADO', 'CANCELADO', 'EXPIRADO'].includes(estado);
  }
}

