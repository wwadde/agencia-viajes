import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PagoService } from '../../../core/services/pago.service';

@Component({
  selector: 'app-checkout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="theme-shell min-h-screen flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full space-y-8 bg-white/70 p-10 rounded-3xl shadow-2xl border border-white/80 text-center relative overflow-hidden">
        
        <!-- Animated Background Blur -->
        <div class="absolute -top-24 -right-24 w-48 h-48 bg-[var(--color-sea)]/60 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-24 -left-24 w-48 h-48 bg-[var(--color-rose)]/40 rounded-full blur-3xl"></div>
        
        <div class="relative z-10">
          <svg class="mx-auto h-16 w-16 text-[var(--color-ink)] mb-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
          <h2 class="text-3xl font-extrabold text-[var(--color-ink)] tracking-tight mb-2">Preparando Pago</h2>
          
          @if (pagoService.loading()) {
            <p class="text-[var(--color-muted)] mb-8">Conectando con la pasarela de pagos, por favor espera un momento...</p>
            <div class="flex justify-center my-8">
              <div class="w-12 h-12 border-4 border-white border-t-[var(--color-ink)] rounded-full animate-spin"></div>
            </div>
            <p class="text-xs text-[var(--color-muted)] mt-4 flex items-center justify-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
              Conexión segura garantizada
            </p>
          } @else if (pagoService.error()) {
            <!-- Error Alert -->
            <div class="bg-red-900/20 border border-red-500/30 rounded-2xl p-6 mb-6">
              <p class="text-red-400 font-medium text-sm">{{ pagoService.error() }}</p>
            </div>
            <div class="flex flex-col gap-3 mt-6">
              <button (click)="iniciarCheckout()" class="w-full py-3 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl transition-colors shadow-lg">
                Reintentar
              </button>
              <a routerLink="/mis-reservas" class="w-full py-3 bg-transparent border border-[var(--color-muted)]/40 hover:border-[var(--color-muted)] text-[var(--color-muted)] hover:text-[var(--color-ink)] font-medium rounded-xl transition-colors">
                Volver a mis reservas
              </a>
            </div>
          } @else if (exitoLocal()) {
             <p class="text-green-400 mb-8 font-medium">Redirigiendo a PayU...</p>
          }
        </div>
      </div>
    </div>
  `
})
export default class CheckoutComponent implements OnInit {
  route = inject(ActivatedRoute);
  pagoService = inject(PagoService);
  
  reservaId = signal<number | null>(null);
  exitoLocal = signal(false);

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.reservaId.set(Number(idParam));
      this.iniciarCheckout();
    }
  }

  async iniciarCheckout() {
    const id = this.reservaId();
    if (!id) return;
    
    // Obtener Intento desde el backend
    const intento = await this.pagoService.crearIntento(id);
    
    if (intento && intento.checkoutPayload) {
      this.exitoLocal.set(true);
      // Analizar JSON PayU
      try {
        const payload = JSON.parse(intento.checkoutPayload);
        
        // Use actionUrl and method from payload if present, fallback to intento
        const actionUrl = payload.actionUrl || intento.urlCheckout;
        const method = payload.method || 'POST';
        const fields = payload.formFields || payload; // fallback just in case backend structure varies
        
        this.submitToPayU(actionUrl, method, fields);
      } catch (e) {
        console.error('Error parseando checkoutPayload de PayU', e);
      }
    }
  }

  /**
   * Crea un formulario hidden en el DOM y lo dispara hacia PayU
   */
  private submitToPayU(actionUrl: string, method: string, fields: Record<string, string>) {
    const form = document.createElement('form');
    form.method = method;
    form.action = actionUrl;
    form.target = '_self'; 
    form.style.display = 'none';

    Object.entries(fields).forEach(([key, value]) => {
        const hiddenField = document.createElement('input');
        hiddenField.type = 'hidden';
        hiddenField.name = key;
        // Se forcea a string para evitar valores undefined que pasen como string literal "undefined"
        hiddenField.value = String(value ?? '');
        form.appendChild(hiddenField);
    });

    document.body.appendChild(form);
    form.submit();
  }
}

