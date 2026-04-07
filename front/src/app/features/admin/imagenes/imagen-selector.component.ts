import { Component, effect, input, output, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ImagenService } from '../../../core/services/imagen.service';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';
import { ImagenBibliotecaItemDTO } from '../../../core/models/imagen.model';

/**
 * Componente modal incrustable para seleccionar imágenes de la biblioteca.
 * Usado dentro de Formularios para campos de imagen.
 */
@Component({
  selector: 'app-imagen-selector',
  standalone: true,
  imports: [CommonModule, FormsModule, ImagenUrlPipe],
  template: `
    @if (isOpen()) {
      <div class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6" (click)="close.emit()">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm transition-opacity"></div>
        
        <!-- Modal Panel -->
        <div class="relative bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl shadow-2xl w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden" (click)="$event.stopPropagation()">
          
          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-[var(--color-sea-strong)]/35 bg-white/50">
            <h3 class="text-lg font-bold text-[var(--color-night)]">Seleccionar Imagen</h3>
            <button (click)="close.emit()" class="text-[var(--color-muted)] hover:text-[var(--color-night)] transition-colors p-2 rounded-lg hover:bg-[var(--color-sea)]/45">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- Toolbar -->
          <div class="p-4 border-b border-[var(--color-sea-strong)]/35 flex gap-4 bg-[var(--color-sea)]/45">
            <select [(ngModel)]="carpeta" (change)="loadImages()" class="bg-[var(--color-sea)]/45 border-none rounded-xl px-4 py-2 text-sm text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] outline-none w-48">
              <option value="">Todas</option>
              <option value="paquetes">Paquetes</option>
              <option value="iconos">Iconos</option>
              <option value="landing">Landing</option>
              <option value="foro">Foro</option>
            </select>
            <input type="text" [(ngModel)]="query" (input)="onSearch()" placeholder="Buscar..." class="flex-1 bg-[var(--color-sea)]/45 border-none rounded-xl px-4 py-2 text-sm text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)] outline-none" />
          </div>

          <!-- Content: Grid / Loading -->
          <div class="flex-1 overflow-y-auto p-6 bg-white/20 relative min-h-[400px]">
             @if (imagenService.loading()) {
              <div class="absolute inset-0 z-10 flex items-center justify-center bg-white/50 backdrop-blur-[2px]">
                 <div class="w-8 h-8 rounded-full border-3 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin"></div>
              </div>
            }

            @if (imagenService.imagenes().length === 0 && !imagenService.loading()) {
              <div class="text-center py-12 text-[var(--color-muted-soft)]">
                <p>No se encontraron imágenes.</p>
              </div>
            } @else {
              <div class="grid grid-cols-3 sm:grid-cols-4 lg:grid-cols-5 gap-4">
                @for (img of imagenService.imagenes(); track img.referencia) {
                  <div class="relative group aspect-square rounded-xl overflow-hidden border-2 cursor-pointer transition-all duration-200"
                       [class.border-blue-500]="selectedRef() === img.referencia"
                       [class.border-transparent]="selectedRef() !== img.referencia"
                       (click)="selectedRef.set(img.referencia)">
                    <img [src]="img.referencia | imagenUrl" loading="lazy" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                    
                    @if (selectedRef() === img.referencia) {
                      <div class="absolute inset-0 bg-blue-500/20 flex items-center justify-center backdrop-blur-[1px]">
                        <div class="w-8 h-8 bg-blue-500 text-white rounded-full flex items-center justify-center shadow-lg transform scale-100 animate-in zoom-in-50 duration-200">
                           <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
                           </svg>
                        </div>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </div>

          <!-- Footer -->
          <div class="px-6 py-4 border-t border-[var(--color-sea-strong)]/35 bg-white/50 flex justify-end gap-3">
            <button (click)="close.emit()" class="px-5 py-2.5 text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-night)] bg-transparent hover:bg-[var(--color-sea)]/45 border border-[var(--color-sea-strong)]/45 hover:border-neutral-600 rounded-xl transition-all">
              Cancelar
            </button>
            <button (click)="confirmSelection()" 
                    [disabled]="!selectedRef()"
                    class="px-5 py-2.5 text-sm font-medium text-white bg-[var(--color-ink)] hover:bg-[var(--color-night)] disabled:opacity-50 disabled:cursor-not-allowed rounded-xl shadow-lg shadow-blue-600/20 disabled:shadow-none transition-all">
              Seleccionar Imagen
            </button>
          </div>

        </div>
      </div>
    }
  `
})
export class ImagenSelectorComponent implements OnInit {
  // Inputs
  isOpen = input(false);
  initialSelection = input<string | null>(null);
  initialFolder = input<string>('');
  
  // Outputs
  close = output<void>();
  selectImage = output<string>();

  imagenService = inject(ImagenService);

  carpeta = '';
  query = '';
  private searchTimeout: any;

  // Local state for selection
  selectedRef = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (!this.isOpen()) {
        return;
      }

      this.selectedRef.set(this.initialSelection());
      if (this.initialFolder()) {
        this.carpeta = this.initialFolder();
      }
      this.loadImages();
    });
  }

  ngOnInit() {
    this.selectedRef.set(this.initialSelection());
    if (this.isOpen()) {
      if (this.initialFolder()) {
        this.carpeta = this.initialFolder();
      }
      this.loadImages();
    }
  }

  loadImages() {
    this.imagenService.loadImagenes(this.carpeta, this.query);
  }

  onSearch() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.loadImages();
    }, 400);
  }

  confirmSelection() {
    const ref = this.selectedRef();
    if (ref) {
      this.selectImage.emit(ref);
      this.close.emit();
    }
  }
}

