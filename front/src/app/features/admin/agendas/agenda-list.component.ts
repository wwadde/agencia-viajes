import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AdminAgendaService } from '../../../core/services/admin-agenda.service';
import { AdminPaqueteService } from '../../../core/services/admin-paquete.service';
import { AgendaSalidaDTO } from '../../../core/models/agenda.model';

@Component({
  selector: 'app-agenda-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Agendas y Salidas</h1>
          <p class="text-[var(--color-muted)] mt-1">Programa las fechas de salida para los paquetes turísticos.</p>
        </div>
        <button (click)="openForm()" class="w-full sm:w-auto px-4 py-2 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-colors flex items-center justify-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          Nueva Salida
        </button>
      </div>

      <div class="flex items-center gap-4">
         <select [(ngModel)]="filtroPaqueteId" (change)="loadAgendas()" class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2 text-[var(--color-night)] outline-none focus:ring-2 focus:ring-[var(--color-ink)] w-full sm:w-64">
            <option [ngValue]="null">Todos los Paquetes</option>
            @for (paq of paqueteService.paquetes(); track paq.id) {
               <option [ngValue]="paq.id">{{ paq.nombre }}</option>
            }
         </select>
      </div>

      <!-- Error Global -->
      @if (agendaService.error() && !showForm()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-[var(--color-night)]">Error</h3>
            <p class="text-sm text-[var(--color-muted)]">{{ agendaService.error() }}</p>
          </div>
          <button (click)="agendaService.clearError()" class="text-[var(--color-muted-soft)] hover:text-[var(--color-night)]"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
      }

      <!-- Grid view of agendamientos -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 relative">
          
        @if (agendaService.loading() && !showForm()) {
          <div class="absolute inset-0 bg-[var(--color-sea)]/45 backdrop-blur-[2px] z-10 flex border border-transparent rounded-2xl items-center justify-center">
             <div class="w-10 h-10 border-4 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] rounded-full animate-spin"></div>
          </div>
        }

        @for (agenda of agendaService.agendas(); track agenda.id) {
          <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl overflow-hidden flex flex-col hover:border-blue-500/30 transition-colors">
             <div class="bg-white border-b border-[var(--color-sea-strong)]/35 p-4">
                <div class="flex items-center justify-between mb-2">
                   <p class="text-xs font-mono text-[var(--color-muted-soft)]">REF-{{ agenda.id }}</p>
                   <span class="px-2 py-1 text-[10px] font-bold uppercase tracking-wider bg-blue-500/10 text-blue-400 rounded-lg border border-blue-500/20">
                     {{ agenda.cuposDisponibles }} cupos
                   </span>
                </div>
                <h3 class="text-sm font-bold text-[var(--color-night)] line-clamp-2" [title]="agenda.paqueteNombre">{{ agenda.paqueteNombre }}</h3>
             </div>
             
             <div class="p-5 flex-1 space-y-4 text-sm">
                <div class="flex justify-between items-center text-[var(--color-muted)]">
                  <span class="text-[var(--color-muted-soft)]">Fecha de salida</span>
                  <span class="font-medium bg-[var(--color-sea)]/45 px-2 py-1 rounded-md">{{ agenda.fechaSalida | date:'mediumDate' }}</span>
                </div>
                <div class="flex justify-between items-center text-[var(--color-muted)]">
                  <span class="text-[var(--color-muted-soft)]">Precio p/p</span>
                  <span class="font-bold text-[var(--color-night)]">{{ agenda.precio | currency:'USD' }}</span>
                </div>
             </div>

             <div class="p-4 border-t border-[var(--color-sea-strong)]/35 flex justify-end gap-2 bg-[var(--color-ink)]">
                <button (click)="openForm(agenda)" class="p-2 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-[var(--color-night)] rounded-lg transition-colors border border-[var(--color-sea-strong)]/45" title="Editar">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                </button>
                <button (click)="deleteAgenda(agenda.id)" class="p-2 bg-red-900/20 hover:bg-red-500/20 text-red-500 hover:text-red-400 rounded-lg transition-colors border border-[var(--color-sea-strong)]/45 hover:border-red-500/40" title="Eliminar">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                </button>
             </div>
          </div>
        } @empty {
           <div class="col-span-full bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl p-12 text-center text-[var(--color-muted-soft)]">
              No se encontraron salidas programadas. 
           </div>
        }
      </div>

      <!-- Slide-over Form -->
      @if (showForm()) {
        <div class="fixed inset-0 z-50 overflow-hidden" aria-labelledby="slide-over-title" role="dialog" aria-modal="true">
          <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity" (click)="closeForm()"></div>
          
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-10">
            <div class="pointer-events-auto w-screen max-w-md transform transition-transform duration-300 ease-in-out">
              <div class="flex h-full flex-col overflow-y-scroll bg-white/90 border-l border-[var(--color-sea-strong)]/35 shadow-2xl">
                <div class="px-6 py-6 border-b border-[var(--color-sea-strong)]/35 bg-[var(--color-sea)]/45 flex items-center justify-between">
                  <h2 class="text-xl font-bold text-[var(--color-night)]" id="slide-over-title">
                    {{ editingId() ? 'Editar Salida' : 'Programar Salida' }}
                  </h2>
                  <button type="button" (click)="closeForm()" class="rounded-lg p-2 text-[var(--color-muted)] hover:text-[var(--color-night)] hover:bg-[var(--color-sea)]/45 transition-colors">
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                  </button>
                </div>
                
                <div class="relative flex-1 px-6 py-6 sm:px-6">
                  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
                    
                    @if (agendaService.error()) {
                      <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4">
                        <p class="text-sm font-medium text-red-400">{{ agendaService.error() }}</p>
                      </div>
                    }

                    <div>
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Paquete Turístico</label>
                      <select formControlName="paqueteId" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all cursor-pointer">
                        <option [ngValue]="null" disabled>Seleccione un paquete...</option>
                        @for (paq of paqueteService.paquetes(); track paq.id) {
                          <option [ngValue]="paq.id">{{ paq.nombre }}</option>
                        }
                      </select>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                       <div class="col-span-2">
                         <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Fecha de Salida</label>
                         <input type="datetime-local" formControlName="fechaSalida" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" />
                       </div>

                       <div>
                         <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Precio (USD)</label>
                         <input type="number" formControlName="precio" min="0" step="0.01" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" />
                       </div>

                       <div>
                         <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Cupos Disponibles</label>
                         <input type="number" formControlName="cuposDisponibles" min="0" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" />
                       </div>
                    </div>

                    <div class="pt-6 border-t border-[var(--color-sea-strong)]/35 mt-8 flex gap-3">
                      <button type="button" (click)="closeForm()" class="flex-1 px-4 py-2.5 bg-transparent border border-[var(--color-sea-strong)]/45 hover:border-[var(--color-sea-strong)]/800 text-[var(--color-muted)] hover:text-[var(--color-night)] font-medium rounded-xl transition-colors">
                        Cancelar
                      </button>
                      <button type="submit" [disabled]="form.invalid || agendaService.loading()" class="flex-1 px-4 py-2.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-all flex items-center justify-center">
                        @if (agendaService.loading()) {
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
export default class AgendaListComponent implements OnInit {
  agendaService = inject(AdminAgendaService);
  paqueteService = inject(AdminPaqueteService);
  fb = inject(FormBuilder);

  filtroPaqueteId: number | null = null;
  showForm = signal(false);
  editingId = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    paqueteId: [null, Validators.required],
    fechaSalida: ['', Validators.required],
    precio: [0, [Validators.required, Validators.min(0)]],
    cuposDisponibles: [0, [Validators.required, Validators.min(0)]]
  });

  ngOnInit() {
    this.paqueteService.loadPaquetes(); // Preload for dropdowns
    this.loadAgendas();
  }

  loadAgendas() {
      // @ts-ignore
      this.agendaService.loadAgendas(this.filtroPaqueteId !== 'null' ? this.filtroPaqueteId : undefined);
  }

  openForm(agenda?: AgendaSalidaDTO) {
    this.agendaService.clearError();
    if (agenda) {
      this.editingId.set(agenda.id);
      
      // format date for datetime-local
      const dateStr = agenda.fechaSalida;
      // Truncate at minutes if needed, but assuming ISO string: YYYY-MM-DDTHH:mm
      const formattedDate = dateStr.length > 16 ? dateStr.substring(0, 16) : dateStr;

      this.form.patchValue({
        paqueteId: agenda.paqueteId,
        fechaSalida: formattedDate,
        precio: agenda.precio,
        cuposDisponibles: agenda.cuposDisponibles
      });
    } else {
      this.editingId.set(null);
      this.form.reset({
          paqueteId: this.filtroPaqueteId !== null && this.filtroPaqueteId !== undefined ? this.filtroPaqueteId : null,
          precio: 0,
          cuposDisponibles: 0
      });
    }
    this.showForm.set(true);
  }

  closeForm() {
    this.showForm.set(false);
    this.agendaService.clearError();
  }

  async onSubmit() {
    if (this.form.invalid) return;

    try {
      const id = this.editingId();
      if (id) {
        await this.agendaService.updateAgenda(id, this.form.value);
      } else {
        await this.agendaService.createAgenda(this.form.value);
      }
      this.closeForm();
    } catch (e) {
      // Error handled by service
    }
  }

  async deleteAgenda(id: number) {
    if (confirm('¿Est\u00e1s seguro que deseas eliminar esta salida programada?')) {
      await this.agendaService.deleteAgenda(id);
    }
  }
}

