import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AdminDestinoService } from '../../../core/services/admin-destino.service';
import { DestinoDTO } from '../../../core/models/destino.model';
import { LeafletMapComponent, MapLocation } from '../../../shared/leaflet-map/leaflet-map.component';

@Component({
  selector: 'app-destino-list',
  imports: [CommonModule, ReactiveFormsModule, LeafletMapComponent],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Destinos</h1>
          <p class="text-[var(--color-muted)] mt-1">Gesti&oacute;n del cat&aacute;logo de destinos.</p>
        </div>
        <button (click)="openForm()" class="w-full sm:w-auto px-4 py-2 bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-colors flex items-center justify-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          Nuevo Destino
        </button>
      </div>

      <!-- Error Global -->
      @if (destinoService.error() && !showForm()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-[var(--color-night)]">Error</h3>
            <p class="text-sm text-[var(--color-muted)]">{{ destinoService.error() }}</p>
          </div>
          <button (click)="destinoService.clearError()" class="text-[var(--color-muted-soft)] hover:text-[var(--color-night)]"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
      }

      <!-- Table -->
      <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-2xl overflow-hidden relative">
        @if (destinoService.loading() && !showForm()) {
          <div class="absolute inset-0 bg-white/50 backdrop-blur-sm z-10 flex items-center justify-center">
            <div class="w-8 h-8 rounded-full border-3 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin"></div>
          </div>
        }

        <div class="overflow-x-auto">
          <table class="w-full min-w-[720px] text-left text-sm text-[var(--color-muted)]">
          <thead class="bg-[var(--color-ink)] text-xs uppercase font-medium text-[var(--color-muted-soft)] border-b border-[var(--color-sea-strong)]/35">
            <tr class="text-white">
              <th scope="col" class="px-6 py-4">ID</th>
              <th scope="col" class="px-6 py-4">Nombre</th>
              <th scope="col" class="px-6 py-4">Ciudad</th>
              <th scope="col" class="px-6 py-4">Pa&iacute;s</th>
              <th scope="col" class="px-6 py-4 text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-neutral-800">
            @for (dest of destinoService.destinos(); track dest.id) {
              <tr class="hover:bg-[var(--color-sea)]/50 transition-colors">
                <td class="px-6 py-4 font-mono text-[var(--color-muted-soft)]">#{{ dest.id }}</td>
                <td class="px-6 py-4 font-medium text-[var(--color-night)]">{{ dest.nombre }}</td>
                <td class="px-6 py-4">{{ dest.ciudad }}</td>
                <td class="px-6 py-4">{{ dest.pais }}</td>
                <td class="px-6 py-4 flex items-center justify-end gap-2">
                  <button (click)="openForm(dest)" class="p-2 text-blue-400 hover:bg-[var(--color-night)]/10 hover:text-blue-300 rounded-lg transition-colors border border-transparent hover:border-blue-500/20" title="Editar">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                  </button>
                  <button (click)="deleteDestino(dest.id)" class="p-2 text-red-400 hover:bg-red-500/10 hover:text-red-300 rounded-lg transition-colors border border-transparent hover:border-red-500/20" title="Eliminar">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                  </button>
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5" class="px-6 py-12 text-center text-[var(--color-muted-soft)]">
                  No se encontraron destinos. Usa el bot&oacute;n "Nuevo Destino" para agregar uno.
                </td>
              </tr>
            }
          </tbody>
          </table>
        </div>
      </div>

      <!-- Slide-over Form -->
      @if (showForm()) {
        <div class="fixed inset-0 z-50 overflow-hidden" aria-labelledby="slide-over-title" role="dialog" aria-modal="true">
          <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity" (click)="closeForm()"></div>
          
          <div class="pointer-events-none fixed inset-y-0 right-0 flex max-w-full pl-0 sm:pl-10">
            <div class="pointer-events-auto w-screen max-w-md transform transition-transform duration-300 ease-in-out">
              <div class="flex h-full flex-col overflow-y-scroll bg-white/90 border-l border-[var(--color-sea-strong)]/35 shadow-2xl">
                <div class="px-6 py-6 border-b border-[var(--color-sea-strong)]/35 bg-[var(--color-sea)]/45 flex items-center justify-between">
                  <h2 class="text-xl font-bold text-[var(--color-night)]" id="slide-over-title">
                    {{ editingId() ? 'Editar Destino' : 'Nuevo Destino' }}
                  </h2>
                  <button type="button" (click)="closeForm()" class="rounded-lg p-2 text-[var(--color-muted)] hover:text-[var(--color-night)] hover:bg-[var(--color-sea)]/45 transition-colors">
                    <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                  </button>
                </div>
                
                <div class="relative flex-1 px-6 py-6 sm:px-6">
                  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-6">
                    
                    @if (destinoService.error()) {
                      <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4">
                        <p class="text-sm font-medium text-red-400">{{ destinoService.error() }}</p>
                      </div>
                    }

                    <div>
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Nombre del Destino</label>
                      <input type="text" formControlName="nombre" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2.5 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" placeholder="Ej: Cancún, París, Roma..." />
                    </div>

                    <div>
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Pa&iacute;s</label>
                      <input type="text" formControlName="pais" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2.5 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" placeholder="Ej: México, Francia, Italia..." />
                    </div>

                    <div>
                      <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Ciudad</label>
                      <input type="text" formControlName="ciudad" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2.5 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" placeholder="Ej: Cancún, Isla Mujeres..." />
                    </div>

                    <fieldset class="space-y-3 border-t border-[var(--color-sea-strong)]/35 pt-5">
                      <legend class="text-sm font-semibold text-[var(--color-night)]">Ubicación</legend>
                      <p class="text-xs text-[var(--color-muted)]">Haz clic en el mapa para seleccionar la ubicación del destino.</p>
                      <app-leaflet-map
                        [latitude]="form.controls['latitud'].value"
                        [longitude]="form.controls['longitud'].value"
                        [interactive]="true"
                        (locationSelected)="onLocationSelected($event)"
                      />
                      <div class="grid grid-cols-2 gap-3">
                        <label class="text-xs text-[var(--color-muted)]">
                          Latitud
                          <input type="number" formControlName="latitud" min="-90" max="90" step="any" class="mt-1 w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-lg px-3 py-2 text-sm text-[var(--color-night)]" />
                        </label>
                        <label class="text-xs text-[var(--color-muted)]">
                          Longitud
                          <input type="number" formControlName="longitud" min="-180" max="180" step="any" class="mt-1 w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-lg px-3 py-2 text-sm text-[var(--color-night)]" />
                        </label>
                      </div>
                      @if (form.controls['latitud'].invalid || form.controls['longitud'].invalid) {
                        <p class="text-xs text-red-600">La latitud debe estar entre -90 y 90 y la longitud entre -180 y 180.</p>
                      }
                    </fieldset>

                    <div class="pt-6 border-t border-[var(--color-sea-strong)]/35 mt-8 flex flex-col sm:flex-row gap-3">
                      <button type="button" (click)="closeForm()" class="flex-1 px-4 py-2.5 bg-transparent border border-[var(--color-sea-strong)]/45 hover:border-[var(--color-sea-strong)]/800 text-[var(--color-muted)] hover:text-[var(--color-night)] font-medium rounded-xl transition-colors">
                        Cancelar
                      </button>
                      <button type="submit" [disabled]="form.invalid || destinoService.loading()" class="flex-1 px-4 py-2.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 transition-all flex items-center justify-center">
                        @if (destinoService.loading()) {
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
export default class DestinoListComponent implements OnInit {
  destinoService = inject(AdminDestinoService);
  fb = inject(FormBuilder);

  showForm = signal(false);
  editingId = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', Validators.required],
    pais: ['', Validators.required],
    ciudad: ['', Validators.required],
    latitud: [null, [Validators.min(-90), Validators.max(90)]],
    longitud: [null, [Validators.min(-180), Validators.max(180)]]
  });

  ngOnInit() {
    this.destinoService.loadDestinos();
  }

  openForm(destino?: DestinoDTO) {
    this.destinoService.clearError();
    if (destino) {
      this.editingId.set(destino.id);
      this.form.patchValue({
        nombre: destino.nombre,
        pais: destino.pais,
        ciudad: destino.ciudad,
        latitud: destino.latitud,
        longitud: destino.longitud
      });
    } else {
      this.editingId.set(null);
      this.form.reset();
    }
    this.showForm.set(true);
  }

  onLocationSelected(location: MapLocation) {
    this.form.patchValue({
      latitud: Number(location.latitude.toFixed(6)),
      longitud: Number(location.longitude.toFixed(6))
    });
  }

  closeForm() {
    this.showForm.set(false);
    this.destinoService.clearError();
  }

  async onSubmit() {
    if (this.form.invalid) return;

    try {
      const id = this.editingId();
      if (id !== null) {
        await this.destinoService.updateDestino(id, this.form.value);
      } else {
        await this.destinoService.createDestino(this.form.value);
      }
      this.closeForm();
    } catch (e) {
      // Error handled by service
    }
  }

  async deleteDestino(id: number) {
    // In a real app we'd use a better confirm dialog, but for now browser confirm is OK
    if (confirm('¿Est\u00e1s seguro que deseas eliminar este destino? Esta acci\u00f3n no se puede deshacer.')) {
      await this.destinoService.deleteDestino(id);
    }
  }
}

