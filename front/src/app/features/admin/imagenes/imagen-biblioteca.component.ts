import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ImagenService } from '../../../core/services/imagen.service';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';
import { ImagenSubidaResponse } from '../../../core/models/imagen.model';

@Component({
  selector: 'app-imagen-biblioteca',
  imports: [CommonModule, FormsModule, ImagenUrlPipe],
  template: `
    <div class="space-y-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Biblioteca de Imágenes</h1>
          <p class="text-[var(--color-muted)] mt-1">Sube, organiza y administra las imágenes disponibles.</p>
        </div>
        
        <div class="flex items-center gap-3 w-full sm:w-auto">
          <!-- Filter/Search -->
          <div class="relative w-full sm:w-64">
            <input type="text" [(ngModel)]="searchQuery" (input)="onSearch()"
                   placeholder="Buscar por nombre..."
                   class="w-full pl-10 pr-4 py-2 bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-xl text-[var(--color-night)] focus:ring-2 focus:ring-[var(--color-ink)]/50 focus:border-[var(--color-ink)] transition-all outline-none" />
            <svg class="absolute left-3 top-2.5 w-5 h-5 text-[var(--color-muted-soft)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <select [(ngModel)]="carpetaFiltro" (change)="loadImages()"
                  class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-xl px-4 py-2 text-[var(--color-night)] outline-none focus:ring-2 focus:ring-[var(--color-ink)]/50 focus:border-[var(--color-ink)] cursor-pointer">
            <option value="">Todas las carpetas</option>
            <option value="paquetes">Paquetes</option>
            <option value="iconos">Iconos</option>
            <option value="landing">Landing</option>
          </select>
        </div>
      </div>

      <!-- Error State -->
      @if (imagenService.error()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4 flex items-start gap-3">
          <svg class="w-6 h-6 text-red-500 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <div>
            <h3 class="text-sm font-medium text-[var(--color-night)]">Ocurrió un error</h3>
            <p class="text-sm text-[var(--color-muted)] mt-1">{{ imagenService.error() }}</p>
          </div>
          <button (click)="imagenService.clearError()" class="ml-auto text-[var(--color-muted-soft)] hover:text-[var(--color-night)]">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
      }

      <!-- Dropzone -->
      <div class="relative group cursor-pointer"
           (dragenter)="onDragEnter($event)"
           (dragover)="onDragOver($event)"
           (dragleave)="onDragLeave($event)"
           (drop)="onDrop($event)">
        <input type="file" #fileInput (change)="onFileSelected($event)" accept="image/jpeg,image/png,image/webp,image/avif" class="hidden" />
        
        <div class="border-2 border-dashed rounded-2xl p-10 text-center transition-all duration-300"
             [class.border-blue-500]="isDragging"
             [class.bg-blue-500]="isDragging"
             [style.background-opacity]="isDragging ? '0.05' : '1'"
             [class.border-[var(--color-sea-strong)]/35]="!isDragging"
             [class.bg-white/90]="!isDragging"
             [class.hover:border-neutral-600]="!isDragging"
             (click)="fileInput.click()">
          
          @if (uploading()) {
            <div class="flex flex-col items-center">
              <div class="w-12 h-12 rounded-full border-4 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin mb-4"></div>
              <p class="text-[var(--color-night)] font-medium">Subiendo imagen...</p>
              <p class="text-sm text-[var(--color-muted-soft)] mt-1">Por favor espera un momento</p>
            </div>
          } @else {
            <div class="w-16 h-16 rounded-full bg-[var(--color-sea)]/45 flex items-center justify-center mx-auto mb-4 group-hover:scale-110 group-hover:bg-[var(--color-night)]/10 group-hover:text-blue-500 transition-all duration-300">
              <svg class="w-8 h-8 text-[var(--color-muted-soft)] group-hover:text-blue-500 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
              </svg>
            </div>
            <p class="text-lg font-medium text-[var(--color-night)] mb-2">Haz clic para subir o arrastra una imagen</p>
            <p class="text-sm text-[var(--color-muted-soft)]">Admite JPG, PNG, WEBP, AVIF (Máximo 5MB)</p>
            
            <div class="mt-6 flex items-center justify-center gap-4" (click)="$event.stopPropagation()">
              <label class="text-sm text-[var(--color-muted)]">Subir en carpeta:</label>
              <select [(ngModel)]="uploadCarpeta" class="bg-[var(--color-sea)]/45 border-none rounded-lg font-medium text-[var(--color-night)] px-3 py-1.5 focus:ring-2 focus:ring-[var(--color-ink)] outline-none">
                <option value="paquetes">/paquetes</option>
                <option value="iconos">/iconos</option>
                <option value="landing">/landing</option>
              </select>
            </div>
          }
        </div>
      </div>

      <!-- Image Grid -->
      <div class="bg-white/90 border border-[var(--color-sea-strong)]/35 rounded-3xl overflow-hidden p-6 sm:p-8 relative">
        @if (imagenService.loading()) {
          <div class="absolute inset-0 bg-white/60 backdrop-blur-sm z-10 flex items-center justify-center">
             <div class="w-10 h-10 rounded-full border-4 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin"></div>
          </div>
        }

        @if (imagenService.imagenes().length === 0 && !imagenService.loading()) {
          <div class="text-center py-16">
            <svg class="w-16 h-16 text-[var(--color-muted-soft)] mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <p class="text-lg font-medium text-[var(--color-night)]">No hay imágenes en esta carpeta</p>
            <p class="text-sm text-[var(--color-muted-soft)] mt-1">Sube una nueva para comenzar.</p>
          </div>
        } @else {
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
            @for (img of imagenService.imagenes(); track img.referencia) {
              <div class="group relative aspect-square rounded-2xl overflow-hidden border border-[var(--color-sea-strong)]/35 bg-white hover:border-blue-500/50 transition-all duration-300">
                <!-- Image Display -->
                <img [src]="img.referencia | imagenUrl" 
                     [alt]="img.nombre"
                     loading="lazy"
                     class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                
                <!-- Hover Overlay -->
                <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col justify-end p-4">
                  <p class="text-xs font-medium text-[var(--color-night)] truncate" [title]="img.nombre">{{ img.nombre }}</p>
                  <p class="text-[10px] text-[var(--color-muted)] mt-0.5 truncate">{{ formatBytes(img.tamanoBytes) }} • /{{ img.carpeta }}</p>
                  
                  <div class="flex items-center gap-2 mt-3 opacity-0 translate-y-2 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300 delay-75">
                    <button class="flex-1 bg-white/10 hover:bg-white/20 text-[var(--color-night)] rounded-lg py-1.5 text-xs font-semibold backdrop-blur-md transition-colors"
                            (click)="copyUrl(img.referencia)"
                            title="Copiar URL">
                      Copiar
                    </button>
                    <button class="flex-none p-1.5 bg-red-500/20 hover:bg-red-500 text-red-500 hover:text-[var(--color-night)] rounded-lg transition-colors border border-red-500/20 hover:border-red-500"
                            (click)="deleteImage(img.referencia)"
                            title="Eliminar imagen">
                      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>

                <!-- Delete Confirmation Overlay -->
                @if (confirmDeleteRef() === img.referencia) {
                  <div class="absolute inset-0 bg-white/95 backdrop-blur-sm z-20 flex flex-col items-center justify-center p-4 text-center">
                    <p class="text-sm font-bold text-[var(--color-night)] mb-4">¿Eliminar permanentemente?</p>
                    <div class="flex gap-2 w-full">
                      <button class="flex-1 py-2 bg-[var(--color-sea)]/45 hover:bg-[var(--color-sea)]/75 text-[var(--color-night)] text-xs font-medium rounded-lg transition-colors"
                              (click)="confirmDeleteRef.set(null)">
                        Cancelar
                      </button>
                      <button class="flex-1 py-2 bg-red-600 hover:bg-red-500 text-white text-xs font-medium rounded-lg shadow-lg shadow-red-500/25 transition-colors"
                              (click)="executeDelete(img.referencia)">
                        Eliminar
                      </button>
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        }
      </div>
    </div>
  `
})
export default class ImagenBibliotecaComponent implements OnInit {
  imagenService = inject(ImagenService);

  isDragging = false;
  uploading = signal(false);
  uploadCarpeta = 'paquetes';
  
  carpetaFiltro = '';
  searchQuery = '';
  private searchTimeout: any;

  confirmDeleteRef = signal<string | null>(null);

  ngOnInit() {
    this.loadImages();
  }

  loadImages() {
    this.imagenService.loadImagenes(this.carpetaFiltro, this.searchQuery);
  }

  onSearch() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.loadImages();
    }, 400); // debounce
  }

  // --- Drag and Drop Handlers ---
  onDragEnter(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleUpload(files[0]);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleUpload(input.files[0]);
      input.value = ''; // Reset
    }
  }

  async handleUpload(file: File) {
    if (!file.type.startsWith('image/')) {
      alert('Solo se permiten archivos de imagen.');
      return;
    }
    
    // Verify file size locally (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('El archivo supera el tamaño máximo de 5MB.');
      return;
    }

    this.uploading.set(true);
    try {
      await this.imagenService.uploadImagen(file, this.uploadCarpeta);
    } catch (e) {
      // Error handled by service
    } finally {
      this.uploading.set(false);
    }
  }

  // --- Actions ---
  deleteImage(referencia: string) {
    this.confirmDeleteRef.set(referencia);
  }

  async executeDelete(referencia: string) {
    try {
      await this.imagenService.deleteImagen(referencia);
      this.confirmDeleteRef.set(null);
    } catch (e) {
      // Error handled by service
      this.confirmDeleteRef.set(null);
    }
  }

  copyUrl(referencia: string) {
    navigator.clipboard.writeText(referencia).then(() => {
      // Temporary basic feedback
      console.log('Referencia copiada:', referencia);
    });
  }

  // --- Utils ---
  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }
}

