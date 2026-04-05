import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminPaqueteService } from '../../../core/services/admin-paquete.service';
import { AdminDestinoService } from '../../../core/services/admin-destino.service';
import { ImagenSelectorComponent } from '../imagenes/imagen-selector.component';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';
import { DestinoDTO } from '../../../core/models/destino.model';
import { PaqueteDetalleDTO, PaqueteUpsertDTO, ImagenPaqueteUpsertDTO } from '../../../core/models/paquete.model';

@Component({
  selector: 'app-paquete-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ImagenSelectorComponent, ImagenUrlPipe],
  template: `
    <div class="max-w-4xl mx-auto space-y-8 pb-12">
      <!-- Header -->
      <div class="flex items-center gap-4">
        <a routerLink="/admin/paquetes" class="p-2 bg-white/90 border border-[var(--color-sea-strong)]/35 text-[var(--color-muted)] hover:text-[var(--color-night)] hover:border-[var(--color-sea-strong)]/45 rounded-xl transition-colors">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">{{ isEditMode() ? 'Editar Paquete Turístico' : 'Nuevo Paquete Turístico' }}</h1>
          <p class="text-[var(--color-muted)] text-sm mt-1 flex items-center gap-2">
            Administración de paquetes
            @if (isEditMode() && paqueteId()) {
              <span class="text-[var(--color-muted-soft)]">•</span>
              <span class="font-mono text-blue-500">ID: {{ paqueteId() }}</span>
            }
          </p>
        </div>
      </div>

      <!-- Main Layout -->
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- Left Column (Main Info) -->
        <div class="lg:col-span-2 space-y-6">
          
          <!-- Basic Info Card -->
          <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl p-6 sm:p-8 space-y-6">
            <h2 class="text-lg font-bold text-[var(--color-night)] border-b border-[var(--color-sea-strong)]/35 pb-4">Información General</h2>
            
            @if (paqueteService.error() || saveError()) {
              <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4">
                <p class="text-sm font-medium text-red-500">{{ saveError() || paqueteService.error() }}</p>
              </div>
            }

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Nombre del Paquete <span class="text-red-500">*</span></label>
                <input type="text" formControlName="nombre" (input)="generateSlug()"
                       class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" 
                       placeholder="Ej: Aventura en Cancún 5 Días" />
              </div>

              <div>
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Slug (URL)</label>
                <input type="text" formControlName="slug"
                       class="w-full bg-[var(--color-sea)]/45 text-[var(--color-muted-soft)] border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 font-mono text-sm outline-none" 
                       placeholder="aventura-en-cancun" />
                <p class="text-xs text-[var(--color-muted-soft)] mt-1">Se genera automáticamente.</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Duración (Días) <span class="text-red-500">*</span></label>
                <input type="number" formControlName="duracionDias" min="1"
                       class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all" />
              </div>

              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Destino Asociado <span class="text-red-500">*</span></label>
                <select formControlName="destinoId" class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all cursor-pointer">
                  <option [ngValue]="null" disabled>Selecciona un destino...</option>
                  @for (dest of destinoService.destinos(); track dest.id) {
                    <option [ngValue]="dest.id">{{ dest.nombre }} ({{ dest.pais }})</option>
                  }
                </select>
              </div>

              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Descripción Breve <span class="text-red-500">*</span></label>
                <textarea formControlName="descripcion" rows="2"
                          class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all resize-none" 
                          placeholder="Resumen atractivo para la tarjeta del paquete..."></textarea>
              </div>

              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Detalle Completo <span class="text-red-500">*</span></label>
                <textarea formControlName="detalle" rows="6"
                          class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all resize-y" 
                          placeholder="Itinerario, qué incluye, políticas, etc..."></textarea>
              </div>
            </div>
          </div>

          <!-- Images Card -->
          <div formArrayName="imagenes" class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl p-6 sm:p-8 space-y-6">
            <div class="flex items-center justify-between border-b border-[var(--color-sea-strong)]/35 pb-4">
              <h2 class="text-lg font-bold text-[var(--color-night)]">Imágenes</h2>
              <button type="button" (click)="addImageParam()" class="px-3 py-1.5 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-sm font-medium text-[var(--color-night)] rounded-lg transition-colors border border-[var(--color-sea-strong)]/45 flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
                Añadir Imagen
              </button>
            </div>

            <div class="space-y-4">
              @for (imgCtrl of imagenesArr.controls; track $index) {
                <div [formGroupName]="$index" class="flex items-center gap-4 bg-[var(--color-sea)]/45 p-3 rounded-2xl border transition-colors relative"
                     [ngClass]="{'border-blue-500/50 bg-blue-500/5': imgCtrl.get('principal')?.value, 'border-[var(--color-sea-strong)]/35': !imgCtrl.get('principal')?.value}">
                  
                  <!-- Preview -->
                  <div class="w-20 h-20 rounded-xl bg-[var(--color-sea)]/45 overflow-hidden flex-shrink-0 border border-[var(--color-sea-strong)]/45 relative cursor-pointer group"
                       (click)="openImageSelector($index)">
                    @if (imgCtrl.get('referencia')?.value) {
                      <img [src]="imgCtrl.get('referencia')?.value | imagenUrl" class="w-full h-full object-cover group-hover:opacity-50 transition-opacity" />
                    } @else {
                      <div class="w-full h-full flex flex-col items-center justify-center text-[var(--color-muted-soft)] group-hover:text-blue-400 group-hover:bg-[var(--color-night)]/10 transition-colors">
                        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                        <span class="text-[10px] mt-1 font-medium">Buscar</span>
                      </div>
                    }
                  </div>

                  <!-- Controls -->
                  <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div class="col-span-2">
                       <input type="text" formControlName="referencia" readonly placeholder="referencia-de-imagen.jpg"
                             class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-lg px-3 py-2 text-sm text-[var(--color-muted)] outline-none" />
                    </div>
                    <div class="flex items-center gap-2">
                      <label class="text-xs font-medium text-[var(--color-muted-soft)]">Orden:</label>
                      <input type="number" formControlName="orden" class="w-20 bg-white border border-[var(--color-sea-strong)]/35 rounded-lg px-2 py-1.5 text-sm text-[var(--color-night)] focus:ring-1 focus:ring-[var(--color-ink)] outline-none" />
                    </div>
                  </div>

                  <!-- Actions -->
                  <div class="flex flex-col gap-2">
                    <label class="flex items-center gap-2 cursor-pointer bg-white/90 px-2 py-1 rounded-lg border border-[var(--color-sea-strong)]/35 hover:border-blue-500/50 transition-colors">
                      <input type="radio" name="principalRadio" [checked]="imgCtrl.get('principal')?.value" (change)="setPrincipal($index)" class="w-4 h-4 text-blue-600 bg-[var(--color-sea)]/45 border-[var(--color-sea-strong)]/45 
focus:ring-blue-600 focus:ring-offset-neutral-900" />
                      <span class="text-xs font-medium text-[var(--color-muted)]">Principal</span>
                    </label>
                    
                    <button type="button" (click)="removeImageParam($index)" class="flex items-center gap-2 justify-center px-2 py-1 text-xs font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-lg transition-colors border border-transparent hover:border-red-500/20">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                      Quitar
                    </button>
                  </div>
                </div>
              } @empty {
                <div class="text-center py-6 text-[var(--color-muted-soft)] text-sm">
                  No se han seleccionado imágenes. Haz clic en "Añadir Imagen".
                </div>
              }
            </div>
            @if (imagenesArr.length > 0 && !hasPrincipalImage()) {
              <p class="text-xs text-amber-500 font-medium">Debe seleccionar una imagen como Principal.</p>
            }
          </div>

        </div>

        <!-- Right Column (Settings & Actions) -->
        <div class="space-y-6">
          <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl p-6 space-y-6 sticky top-24">
            <h2 class="text-lg font-bold text-[var(--color-night)] border-b border-[var(--color-sea-strong)]/35 pb-4">Visibilidad</h2>
            
            <label class="flex items-center justify-between cursor-pointer p-4 rounded-xl border transition-colors"
                   [ngClass]="form.get('disponible')?.value ? 'border-green-500/50 bg-green-500/5' : 'border-[var(--color-sea-strong)]/35 bg-white'">
              <div>
                <p class="font-medium text-[var(--color-night)]">Disponible</p>
                <p class="text-xs text-[var(--color-muted-soft)] mt-0.5">Permite realizar reservas.</p>
              </div>
              <div class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" formControlName="disponible" class="sr-only peer">
                <div class="w-11 h-6 bg-[var(--color-muted-soft)]/55 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-[var(--color-sea-strong)] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-500"></div>
              </div>
            </label>

            <label class="flex items-center justify-between cursor-pointer p-4 rounded-xl border transition-colors"
                   [ngClass]="form.get('destacado')?.value ? 'border-amber-500/50 bg-amber-500/5' : 'border-[var(--color-sea-strong)]/35 bg-white'">
              <div>
                <p class="font-medium text-[var(--color-night)] flex items-center gap-1.5">
                  <svg class="w-4 h-4 text-amber-500" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" /></svg>
                  Destacado
                </p>
                <p class="text-xs text-[var(--color-muted-soft)] mt-0.5">Prioridad en inicio.</p>
              </div>
              <div class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" formControlName="destacado" class="sr-only peer">
                <div class="w-11 h-6 bg-[var(--color-muted-soft)]/55 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-[var(--color-sea-strong)] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-amber-500"></div>
              </div>
            </label>

            <!-- Actions -->
            <div class="pt-6 border-t border-[var(--color-sea-strong)]/35 space-y-3">
              <button type="submit" [disabled]="form.invalid || !hasPrincipalImage() || isSaving()"
                      class="w-full flex items-center justify-center py-3.5 bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:bg-[var(--color-sea)]/45 disabled:text-[var(--color-muted-soft)] text-white font-bold rounded-xl shadow-lg shadow-[var(--color-sea-strong)]/20 disabled:shadow-none transition-all">
                @if (isSaving()) {
                  <div class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></div>
                }
                {{ isEditMode() ? 'Actualizar Paquete' : 'Crear Paquete' }}
              </button>
              
              <a routerLink="/admin/paquetes" class="w-full flex items-center justify-center py-3 bg-transparent border border-[var(--color-sea-strong)]/45 hover:border-[var(--color-sea-strong)]/800 text-[var(--color-muted)] font-medium rounded-xl transition-colors">
                Cancelar
              </a>
            </div>
          </div>
        </div>
      </form>
    </div>

    <!-- Image Modal -->
    <app-imagen-selector 
      [isOpen]="selectorOpen()" 
      (close)="selectorOpen.set(false)" 
      (selectImage)="onImageSelected($event)">
    </app-imagen-selector>
  `
})
export default class PaqueteFormComponent implements OnInit {
  paqueteService = inject(AdminPaqueteService);
  destinoService = inject(AdminDestinoService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);
  router = inject(Router);

  isEditMode = signal(false);
  paqueteId = signal<number | null>(null);
  isSaving = signal(false);
  saveError = signal<string | null>(null);

  // Selector state
  selectorOpen = signal(false);
  activeImageIndex = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', Validators.required],
    slug: [''], // Auto-generated
    descripcion: ['', Validators.required],
    detalle: ['', Validators.required],
    duracionDias: [1, [Validators.required, Validators.min(1)]],
    disponible: [true],
    destacado: [false],
    destinoId: [null, Validators.required],
    imagenes: this.fb.array([])
  });

  get imagenesArr() {
    return this.form.get('imagenes') as FormArray;
  }

  ngOnInit() {
    this.destinoService.loadDestinos(); // Preload dropdown options

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && idParam !== 'nuevo') {
      this.isEditMode.set(true);
      this.paqueteId.set(+idParam);
      this.loadPaquete(+idParam);
    } else {
      // For new, add one empty image to start
      this.addImageParam();
      // Ensure it's marked as principal
      this.setPrincipal(0);
    }
  }

  async loadPaquete(id: number) {
    const paq = await this.paqueteService.getPaquete(id);
    if (paq) {
      this.form.patchValue({
        nombre: paq.nombre,
        slug: paq.slug,
        descripcion: paq.descripcion,
        detalle: paq.detalle,
        duracionDias: paq.duracionDias,
        disponible: paq.disponible,
        destacado: paq.destacado,
        destinoId: paq.destino.id
      });

      // Populate form array
      if (paq.imagenes && paq.imagenes.length > 0) {
        paq.imagenes.forEach(img => {
          this.imagenesArr.push(this.createImageGroup({
            referencia: img.url, // BE returns DTO with 'url' property, but for Upsert we need 'referencia'
            orden: img.orden,
            principal: img.principal
          }));
        });
      }
    }
  }

  // --- Image FormArray Management ---
  createImageGroup(data?: any): FormGroup {
    return this.fb.group({
      referencia: [data?.referencia || '', Validators.required],
      orden: [data?.orden || 0, Validators.required],
      principal: [data ? data.principal : false]
    });
  }

  addImageParam() {
    const newIndex = this.imagenesArr.length;
    this.imagenesArr.push(this.createImageGroup({ orden: newIndex }));
  }

  removeImageParam(index: number) {
    const wasPrincipal = this.imagenesArr.at(index).get('principal')?.value;
    this.imagenesArr.removeAt(index);
    if (wasPrincipal && this.imagenesArr.length > 0) {
      this.setPrincipal(0);
    }
  }

  setPrincipal(index: number) {
    for (let i = 0; i < this.imagenesArr.length; i++) {
      this.imagenesArr.at(i).get('principal')?.setValue(i === index);
    }
  }

  hasPrincipalImage(): boolean {
    if (this.imagenesArr.length === 0) return true; // valid if empty? actually Backend might validate it.
    for (let i = 0; i < this.imagenesArr.length; i++) {
      if (this.imagenesArr.at(i).get('principal')?.value) return true;
    }
    return false;
  }

  // --- Image Selector Modal ---
  openImageSelector(index: number) {
    this.activeImageIndex.set(index);
    this.selectorOpen.set(true);
  }

  onImageSelected(referencia: string) {
    const idx = this.activeImageIndex();
    if (idx !== null) {
      this.imagenesArr.at(idx).get('referencia')?.setValue(referencia);
    }
  }

  // --- Utils ---
  generateSlug() {
    if (!this.isEditMode()) {
      const nombre = this.form.get('nombre')?.value || '';
      const slug = nombre.toLowerCase()
        .replace(/[^\w\s-]/g, '')
        .replace(/\s+/g, '-');
      this.form.get('slug')?.setValue(slug, { emitEvent: false });
    }
  }

  // --- Submit ---
  async onSubmit() {
    if (this.form.invalid || !this.hasPrincipalImage()) return;
    
    this.isSaving.set(true);
    this.saveError.set(null);

    const data: PaqueteUpsertDTO = {
      ...this.form.value
    };

    try {
      if (this.isEditMode() && this.paqueteId()) {
        await this.paqueteService.updatePaquete(this.paqueteId()!, data);
      } else {
        await this.paqueteService.createPaquete(data);
      }
      this.router.navigate(['/admin/paquetes']);
    } catch (e) {
      this.saveError.set('No se pudo guardar el paquete. Verifica los datos.');
    } finally {
      this.isSaving.set(false);
    }
  }
}

