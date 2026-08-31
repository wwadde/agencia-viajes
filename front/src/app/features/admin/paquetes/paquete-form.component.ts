import { Component, OnDestroy, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { generateText, JSONContent } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import { AdminPaqueteService } from '../../../core/services/admin-paquete.service';
import { AdminDestinoService } from '../../../core/services/admin-destino.service';
import { ImagenSelectorComponent } from '../imagenes/imagen-selector.component';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';
import { DestinoDTO } from '../../../core/models/destino.model';
import {
  ImagenPaqueteUpsertDTO,
  PaqueteDetalleContent,
  PaqueteUpsertDTO
} from '../../../core/models/paquete.model';
import { ProblemDetail } from '../../../core/models/error.model';
import { RichTextEditorComponent } from '../../../shared/rich-text-editor/rich-text-editor.component';

@Component({
  selector: 'app-paquete-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ImagenSelectorComponent, ImagenUrlPipe, RichTextEditorComponent],
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
                <div class="flex items-center justify-between gap-3 mb-1.5">
                  <label class="block text-sm font-medium text-[var(--color-muted)]">Ruta del paquete <span class="text-red-500">*</span></label>
                  <span class="text-xs text-[var(--color-muted-soft)]">{{ selectedDestinoIds().length }} destinos</span>
                </div>
                <div class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-[var(--color-sea)]/20 p-3 space-y-2">
                  @for (dest of selectedDestinos(); track dest.id; let index = $index) {
                    <div class="flex items-center gap-3 rounded-xl bg-white border border-[var(--color-sea-strong)]/30 px-3 py-2">
                      <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[var(--color-ink)] text-xs font-bold text-white">{{ index + 1 }}</span>
                      <div class="min-w-0 flex-1">
                        <p class="truncate text-sm font-semibold text-[var(--color-night)]">{{ dest.nombre }}</p>
                        <p class="truncate text-xs text-[var(--color-muted-soft)]">{{ dest.ciudad }}, {{ dest.pais }}</p>
                      </div>
                      <button type="button" (click)="moveDestino(index, -1)" [disabled]="index === 0" class="p-1.5 text-[var(--color-muted)] disabled:opacity-30" title="Subir destino" aria-label="Subir destino">↑</button>
                      <button type="button" (click)="moveDestino(index, 1)" [disabled]="index === selectedDestinos().length - 1" class="p-1.5 text-[var(--color-muted)] disabled:opacity-30" title="Bajar destino" aria-label="Bajar destino">↓</button>
                      <button type="button" (click)="removeDestino(dest.id)" class="p-1.5 text-red-500" title="Quitar destino" aria-label="Quitar destino">×</button>
                    </div>
                  } @empty {
                    <p class="px-2 py-1 text-sm text-[var(--color-muted-soft)]">Selecciona los lugares que formarán el recorrido.</p>
                  }
                </div>
                <div class="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-56 overflow-y-auto pr-1">
                  <input type="search" [value]="destinoSearch()" (input)="destinoSearch.set($any($event.target).value)" placeholder="Buscar por nombre, ciudad o país..." aria-label="Buscar destinos" class="sm:col-span-2 w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-3 py-2.5 text-sm text-[var(--color-night)] outline-none focus:ring-2 focus:ring-[var(--color-ink)]" />
                  @for (dest of filteredDestinos(); track dest.id) {
                    <label class="flex cursor-pointer items-center gap-3 rounded-xl border px-3 py-2 transition-colors"
                           [class.border-[var(--color-ink)]]="isDestinoSelected(dest.id)"
                           [class.bg-[var(--color-sea)]]="isDestinoSelected(dest.id)"
                           [class.border-[var(--color-sea-strong)]/35]="!isDestinoSelected(dest.id)">
                      <input type="checkbox" [checked]="isDestinoSelected(dest.id)" (change)="toggleDestino(dest.id, $any($event.target).checked)" class="h-4 w-4 accent-[var(--color-ink)]" />
                      <span class="min-w-0"><span class="block truncate text-sm font-medium text-[var(--color-night)]">{{ dest.nombre }}</span><span class="block truncate text-xs text-[var(--color-muted-soft)]">{{ dest.ciudad }}, {{ dest.pais }}</span></span>
                    </label>
                  }
                  @empty {
                    <p class="sm:col-span-2 px-2 py-3 text-sm text-[var(--color-muted-soft)]">No hay destinos que coincidan con la búsqueda.</p>
                  }
                </div>
              </div>

              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Descripción Breve <span class="text-red-500">*</span></label>
                <textarea formControlName="descripcion" rows="2"
                          class="w-full bg-white border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-3 text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] focus:border-[var(--color-ink)] outline-none transition-all resize-none" 
                          placeholder="Resumen atractivo para la tarjeta del paquete..."></textarea>
              </div>

              <div class="sm:col-span-2">
                <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Detalle Completo <span class="text-red-500">*</span></label>
                <app-rich-text-editor formControlName="detalle"></app-rich-text-editor>
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

    @if (alertError()) {
      <div class="fixed top-6 right-6 z-[70] w-full max-w-sm rounded-2xl border border-red-500/40 bg-red-900/95 p-4 shadow-2xl shadow-red-900/45">
        <div class="flex items-start gap-3">
          <svg class="h-6 w-6 flex-shrink-0 text-red-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div class="flex-1">
            <h4 class="text-sm font-bold text-white">No se pudo guardar el paquete</h4>
            <p class="mt-1 text-xs text-red-100">{{ alertError() }}</p>
          </div>
          <button
            type="button"
            (click)="clearAlertError()"
            class="text-red-200 transition-colors hover:text-white"
            aria-label="Cerrar alerta"
            title="Cerrar alerta">
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
      </div>
    }
  `
})
export default class PaqueteFormComponent implements OnInit, OnDestroy {
  paqueteService = inject(AdminPaqueteService);
  destinoService = inject(AdminDestinoService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);
  router = inject(Router);

  isEditMode = signal(false);
  paqueteId = signal<number | null>(null);
  isSaving = signal(false);
  saveError = signal<string | null>(null);
  alertError = signal<string | null>(null);
  private alertTimeoutId: ReturnType<typeof setTimeout> | null = null;
  selectedDestinoIds = signal<number[]>([]);
  destinoSearch = signal('');
  filteredDestinos = computed(() => {
    const query = this.destinoSearch().trim().toLowerCase();
    if (!query) {
      return this.destinoService.destinos();
    }
    return this.destinoService.destinos().filter(destino =>
      `${destino.nombre} ${destino.ciudad} ${destino.pais}`.toLowerCase().includes(query));
  });
  selectedDestinos = computed(() => this.selectedDestinoIds()
    .map(id => this.destinoService.destinos().find(destino => destino.id === id))
    .filter((destino): destino is DestinoDTO => Boolean(destino)));

  // Selector state
  selectorOpen = signal(false);
  activeImageIndex = signal<number | null>(null);

  form: FormGroup = this.fb.group({
    nombre: ['', Validators.required],
    slug: [''], // Auto-generated
    descripcion: ['', Validators.required],
    detalle: [this.emptyDetalle(), Validators.required],
    duracionDias: [1, [Validators.required, Validators.min(1)]],
    disponible: [true],
    destacado: [false],
    destinoIds: [[], Validators.required],
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

  ngOnDestroy(): void {
    this.clearAlertError();
  }

  async loadPaquete(id: number) {
    const paq = await this.paqueteService.getPaquete(id);
    if (paq) {
      this.form.patchValue({
        nombre: paq.nombre,
        slug: paq.slug,
        descripcion: paq.descripcion,
        detalle: this.normalizeDetalle(paq.detalle),
        duracionDias: paq.duracionDias,
        disponible: paq.disponible,
        destacado: paq.destacado,
        destinoIds: (paq.destinos?.length ? paq.destinos : [paq.destino]).map(destino => destino.id)
      });
      this.selectedDestinoIds.set(this.form.get('destinoIds')?.value ?? []);

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

  isDestinoSelected(id: number): boolean {
    return this.selectedDestinoIds().includes(id);
  }

  toggleDestino(id: number, selected: boolean): void {
    const ids = this.selectedDestinoIds();
    const next = selected ? [...ids, id] : ids.filter(existingId => existingId !== id);
    this.selectedDestinoIds.set(next);
    this.form.get('destinoIds')?.setValue(next);
    this.form.get('destinoIds')?.markAsTouched();
  }

  removeDestino(id: number): void {
    this.toggleDestino(id, false);
  }

  moveDestino(index: number, offset: number): void {
    const next = [...this.selectedDestinoIds()];
    const target = index + offset;
    if (target < 0 || target >= next.length) {
      return;
    }
    [next[index], next[target]] = [next[target], next[index]];
    this.selectedDestinoIds.set(next);
    this.form.get('destinoIds')?.setValue(next);
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
      const slug = nombre
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-+|-+$/g, '');
      this.form.get('slug')?.setValue(slug, { emitEvent: false });
    }
  }

  // --- Submit ---
  async onSubmit() {
    if (this.form.invalid || !this.hasPrincipalImage()) return;
    
    this.isSaving.set(true);
    this.saveError.set(null);
    this.clearAlertError();

    const value = this.form.getRawValue();
    const data: PaqueteUpsertDTO = {
      ...value,
      detalle: this.normalizeDetalle(value.detalle as PaqueteDetalleContent),
        detalleTextoPlano: this.extractPlainText(value.detalle as PaqueteDetalleContent),
        destinoIds: value.destinoIds as number[],
      imagenes: value.imagenes as ImagenPaqueteUpsertDTO[]
    };

    try {
      if (this.isEditMode() && this.paqueteId()) {
        await this.paqueteService.updatePaquete(this.paqueteId()!, data);
      } else {
        await this.paqueteService.createPaquete(data);
      }
      this.router.navigate(['/admin/paquetes']);
    } catch (e) {
      const message = this.extractProblemMessage(e, 'No se pudo guardar el paquete. Verifica los datos.');
      this.saveError.set(message);
      this.showAlertError(message);
    } finally {
      this.isSaving.set(false);
    }
  }

  private showAlertError(message: string): void {
    this.alertError.set(message);
    if (this.alertTimeoutId !== null) {
      clearTimeout(this.alertTimeoutId);
    }
    this.alertTimeoutId = setTimeout(() => {
      this.alertError.set(null);
      this.alertTimeoutId = null;
    }, 6000);
  }

  clearAlertError(): void {
    this.alertError.set(null);
    if (this.alertTimeoutId !== null) {
      clearTimeout(this.alertTimeoutId);
      this.alertTimeoutId = null;
    }
  }

  private extractProblemMessage(error: unknown, fallbackMessage: string): string {
    if (error instanceof HttpErrorResponse) {
      const problem = error.error as ProblemDetail | null;
      const violationMessage = problem?.violations
        ?.map(v => v?.mensaje?.trim())
        .find((mensaje): mensaje is string => Boolean(mensaje));
      if (violationMessage) {
        return violationMessage;
      }
      if (problem?.detail?.trim()) {
        return problem.detail.trim();
      }
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error.trim();
      }
    }
    return fallbackMessage;
  }

  private normalizeDetalle(detalle: PaqueteDetalleContent): JSONContent {
    if (this.isJsonContent(detalle)) {
      return detalle;
    }

    if (typeof detalle === 'string') {
      const trimmed = detalle.trim();
      if (!trimmed) {
        return this.emptyDetalle();
      }

      const parsed = this.tryParseJsonContent(trimmed);
      if (parsed) {
        return parsed;
      }

      return {
        type: 'doc',
        content: [
          {
            type: 'paragraph',
            content: [{ type: 'text', text: trimmed }]
          }
        ]
      };
    }

    return this.emptyDetalle();
  }

  private tryParseJsonContent(value: string): JSONContent | null {
    try {
      const parsed: unknown = JSON.parse(value);
      return this.isJsonContent(parsed) ? parsed : null;
    } catch {
      return null;
    }
  }

  private isJsonContent(value: unknown): value is JSONContent {
    if (typeof value !== 'object' || value === null) {
      return false;
    }
    const withType = value as { type?: unknown };
    return typeof withType.type === 'string';
  }

  private extractPlainText(detalle: PaqueteDetalleContent): string {
    if (!this.isJsonContent(detalle)) {
      return '';
    }
    return generateText(detalle, [StarterKit])
      .replace(/[ \t]+/g, ' ')
      .replace(/ *\n */g, '\n')
      .trim();
  }

  private emptyDetalle(): JSONContent {
    return {
      type: 'doc',
      content: [{ type: 'paragraph' }]
    };
  }
}
