import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AdminContenidoService } from '../../../core/services/admin-contenido.service';
import { ContenidoDTO } from '../../../core/models/contenido.model';

@Component({
  selector: 'app-contenido-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Contenido Dinámico</h1>
          <p class="text-[var(--color-muted)] mt-1">Textos y configuraciones para la página pública.</p>
        </div>
        <button (click)="openForm()" class="w-full sm:w-auto px-4 py-2 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-colors flex items-center justify-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          Nueva Clave
        </button>
      </div>

      <!-- Error Global -->
      @if (contenidoService.error() && !showForm()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-[var(--color-night)]">Error</h3>
            <p class="text-sm text-[var(--color-muted)]">{{ contenidoService.error() }}</p>
          </div>
          <button (click)="contenidoService.clearError()" class="text-[var(--color-muted-soft)] hover:text-[var(--color-night)]"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
      }

      <!-- Grid view of Contenidos -->
      <!-- Since "contenidos" usually have a large text value, displaying them in a grid of cards is nice -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 relative">
          
        @if (contenidoService.loading() && !showForm()) {
          <div class="absolute inset-0 bg-[var(--color-sea)]/45 backdrop-blur-[2px] z-10 flex border border-transparent rounded-2xl items-center justify-center">
             <div class="w-10 h-10 border-4 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] rounded-full animate-spin"></div>
          </div>
        }

        @for (cont of contenidoService.contenidos(); track cont.id) {
          <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl overflow-hidden flex flex-col hover:border-[var(--color-sea-strong)]/45 transition-colors">
             <div class="bg-white border-b border-[var(--color-sea-strong)]/35 p-4 flex items-center justify-between">
                <span class="px-2.5 py-1 text-xs font-mono font-medium text-blue-400 bg-blue-500/10 border border-blue-500/20 rounded-lg">{{ cont.clave }}</span>
                <span class="text-xs text-[var(--color-muted-soft)]">ID: {{ cont.id }}</span>
             </div>
             <div class="p-5 flex-1 max-h-48 overflow-y-auto custom-scrollbar">
                <p class="text-sm text-[var(--color-muted)] whitespace-pre-wrap">{{ cont.valor || '(Vacio)' }}</p>
             </div>
             <div class="p-4 border-t border-[var(--color-sea-strong)]/35 flex justify-end gap-2 bg-[var(--color-ink)]">
                <button (click)="openForm(cont)" class="p-2 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-[var(--color-night)] rounded-lg transition-colors border border-[var(--color-sea-strong)]/45" title="Editar">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                </button>
                <button (click)="deleteContenido(cont.id)" class="p-2 bg-red-900/20 hover:bg-red-500/20 text-red-500 hover:text-red-400 rounded-lg transition-colors border border-[var(--color-sea-strong)]/45 hover:border-red-500/40" title="Eliminar">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                </button>
             </div>
          </div>
        } @empty {
           <div class="col-span-full bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl p-12 text-center text-[var(--color-muted-soft)]">
              No se encontraron contenidos dinámicos. Crea uno nuevo para configurar textos del sitio.
           </div>
        }
      </div>

      <!-- Slide-over Form -->
      @if (showForm()) {
        <div class="fixed inset-0 z-50 overflow-hidden" aria-labelledby="slide-over-title" role="dialog" aria-modal="true">
          <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity" (click)="closeForm()"></div>
          
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
            <div class="pointer-events-auto w-screen max-w-md transform transition-transform duration-300 ease-in-out">
              <div class="flex h-full flex-col bg-white/90 border-l border-[var(--color-sea-strong)]/35 shadow-2xl">
                <div class="px-6 py-6 border-b border-[var(--color-sea-strong)]/35 bg-[var(--color-sea)]/45 flex items-center justify-between">
                  <h2 class="text-xl font-bold text-[var(--color-night)]" id="slide-over-title">
                    {{ editingId() ? 'Editar Contenido' : 'Nuevo Contenido' }}
                  </h2>
                  <button type="button" (click)="closeForm()" class="rounded-lg p-2 text-[var(--color-muted)] hover:text-[var(--color-night)] hover:bg-[var(--color-sea)]/45 transition-colors">
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                  </button>
                </div>
                
                <div class="relative flex-1 px-6 py-6 sm:px-6 overflow-y-auto">
                  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6 flex flex-col h-full">
                    
                    @if (contenidoService.error()) {
                      <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4">
                        <p class="text-sm font-medium text-red-400">{{ contenidoService.error() }}</p>
                      </div>
                    }

                    <div class="shrink-0">
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Clave de Referencia</label>
                      <input type="text" formControlName="clave" 
                             [attr.disabled]="editingId() ? true : null"
                             [ngClass]="editingId() ? 'bg-white/70 text-[var(--color-muted-soft)] border-[var(--color-sea-strong)]/35 cursor-not-allowed' : 'bg-white text-[var(--color-night)] border-[var(--color-sea-strong)]/35 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)]'"
                             class="w-full border rounded-xl px-4 py-3 font-mono text-sm outline-none transition-all" 
                             placeholder="ej: footer_description" />
                      @if (editingId()) {
                         <p class="text-xs text-[var(--color-muted-soft)] mt-1">La clave no se puede modificar.</p>
                      }
                    </div>

                    <div class="flex-1 flex flex-col">
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Valor / Texto</label>
                      <textarea formControlName="valor" 
                                class="w-full flex-1 bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all resize-none min-h-[300px]" 
                                placeholder="Escribe el texto enriquecido o HTML aquí..."></textarea>
                    </div>

                    <div class="pt-6 border-t border-[var(--color-sea-strong)]/35 mt-auto shrink-0 flex gap-3">
                      <button type="button" (click)="closeForm()" class="flex-1 px-4 py-2.5 bg-transparent border border-[var(--color-sea-strong)]/45 hover:border-[var(--color-sea-strong)]/800 text-[var(--color-muted)] hover:text-[var(--color-night)] font-medium rounded-xl transition-colors">
                        Cancelar
                      </button>
                      <button type="submit" [disabled]="form.invalid || contenidoService.loading()" class="flex-1 px-4 py-2.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-all flex items-center justify-center">
                        @if (contenidoService.loading()) {
                          <div class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                        }
                        Guardar
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      }

    </div>
  `
})
export default class ContenidoListComponent implements OnInit {
  contenidoService = inject(AdminContenidoService);
  fb = inject(FormBuilder);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    clave: [{value: '', disabled: false}, Validators.required],
    valor: [''] // It's optional in DTO
  });

  ngOnInit() {
    this.contenidoService.loadContenidos();
  }

  openForm(cont?: ContenidoDTO) {
    this.contenidoService.clearError();
    if (cont) {
      this.editingId.set(cont.id);
      this.form.patchValue({
        clave: cont.clave,
        valor: cont.valor
      });
      // The attr.disabled in template restricts editing visually, but reactive forms need explicit disable for actual lock:
      this.form.get('clave')?.disable();
    } else {
      this.editingId.set(null);
      this.form.get('clave')?.enable();
      this.form.reset();
    }
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.contenidoService.clearError();
  }

  async onSubmit() {
    if (this.form.invalid) return;

    try {
      const id = this.editingId();
      if (id) {
        // use getRawValue to include disabled fields
        await this.contenidoService.updateContenido(id, this.form.getRawValue());
      } else {
        await this.contenidoService.createContenido(this.form.value);
      }
      this.closeForm();
    } catch (e) {
      // Error handled by service
    }
  }

  async deleteContenido(id: number) {
    if (confirm('¿Est\u00e1s seguro que deseas eliminar este contenido?')) {
      await this.contenidoService.deleteContenido(id);
    }
  }
}

