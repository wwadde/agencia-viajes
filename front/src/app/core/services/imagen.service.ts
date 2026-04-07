import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ImagenBibliotecaItemDTO, ImagenSubidaResponse } from '../models/imagen.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class ImagenService {
    private readonly baseUrl = `${environment.apiUrl}/admin/imagenes`;
    private readonly thumbnailDirectory = 'miniaturas';
    private readonly smallThumbnailWidth = 480;
    private readonly mediumThumbnailWidth = 960;
    private http = inject(HttpClient);

    // Private writable signals
    private _imagenes = signal<ImagenBibliotecaItemDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    // Public readonly signals
    readonly imagenes = this._imagenes.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    /**
     * Construye la URL de visualización final a partir de la referencia relativa
     * según el entorno actual:
     * - desarrollo: /images/ (proxy a backend local)
     * - producción: /images/ (Nginx sirviendo archivos estáticos)
     */
    buildUrl(referencia: string): string {
        if (!referencia) return '';
        const normalized = referencia.replace(/\\/g, '/');

        if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
            try {
                const url = new URL(normalized);
                // Si el backend devuelve URL absoluta local para imágenes, usar ruta relativa
                // para evitar advertencias de preconnect en desarrollo.
                if (url.pathname.startsWith('/images/') || url.pathname.startsWith('/imagenes/')) {
                    return url.pathname;
                }
            } catch {
                return normalized;
            }

            return normalized;
        }

        // Si backend ya devolvió una ruta absoluta del sitio, no volver a prefijar.
        if (normalized.startsWith('/')) {
            return normalized;
        }

        // Compatibilidad con referencias legacy que ya incluyen prefijo.
        if (normalized.startsWith('images/') || normalized.startsWith('imagenes/')) {
            return `/${normalized}`;
        }

        const base = environment.imageBaseUrl.replace(/\/$/, '');
        return `${base}/${normalized}`;
    }

    buildThumbnailUrl(referencia: string, width: number): string {
        if (!referencia) return '';
        const thumbnailReference = this.toThumbnailReference(referencia, width);
        return this.buildUrl(thumbnailReference);
    }

    buildMobileSrcset(referencia: string): string {
        if (!referencia) return '';

        const small = this.buildThumbnailUrl(referencia, this.smallThumbnailWidth);
        const medium = this.buildThumbnailUrl(referencia, this.mediumThumbnailWidth);

        return `${small} ${this.smallThumbnailWidth}w, ${medium} ${this.mediumThumbnailWidth}w`;
    }

    private toThumbnailReference(referencia: string, width: number): string {
        const normalized = this.normalizeReferenceForThumbnail(referencia);
        if (!normalized) {
            return referencia;
        }

        const slashIndex = normalized.lastIndexOf('/');
        const dotIndex = normalized.lastIndexOf('.');
        if (slashIndex < 0 || dotIndex <= slashIndex) {
            return normalized;
        }

        const fileName = normalized.slice(slashIndex + 1, dotIndex);
        const extension = normalized.slice(dotIndex);
        const baseName = fileName.replace(/_(\d+)w$/, '');

        return `${this.thumbnailDirectory}/${baseName}_${width}w${extension}`;
    }

    private normalizeReferenceForThumbnail(referencia: string): string | null {
        if (!referencia) {
            return null;
        }

        let normalized = referencia.replace(/\\/g, '/').trim();

        if (normalized.startsWith('http://') || normalized.startsWith('https://')) {
            try {
                normalized = new URL(normalized).pathname;
            } catch {
                return null;
            }
        }

        if (normalized.startsWith('/')) {
            normalized = normalized.slice(1);
        }

        if (normalized.startsWith('images/')) {
            normalized = normalized.slice('images/'.length);
        }

        if (normalized.startsWith('imagenes/')) {
            normalized = normalized.slice('imagenes/'.length);
        }

        if (!normalized.includes('/')) {
            return null;
        }

        return normalized;
    }

    async loadImagenes(carpeta: string = '', query: string = ''): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        
        let params = new HttpParams();
        if (carpeta) params = params.set('carpeta', carpeta);
        if (query) params = params.set('q', query);

        try {
            const result = await firstValueFrom(
                this.http.get<ImagenBibliotecaItemDTO[]>(this.baseUrl, { params })
            );
            this._imagenes.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar la biblioteca de imágenes');
            this._imagenes.set([]);
        } finally {
            this._loading.set(false);
        }
    }

    async uploadImagen(file: File, carpeta: string): Promise<ImagenSubidaResponse> {
        this._loading.set(true);
        this._error.set(null);

        const formData = new FormData();
        formData.append('file', file);

        let params = new HttpParams();
        if (carpeta) params = params.set('carpeta', carpeta);

        try {
            const response = await firstValueFrom(
                this.http.post<ImagenSubidaResponse>(this.baseUrl, formData, { params })
            );
            // Recargar para ver el cambio
            await this.loadImagenes(carpeta);
            return response;
        } catch (e) {
            this.handleError(e, 'Error al subir la imagen');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async deleteImagen(referencia: string): Promise<void> {
        this._loading.set(true);
        this._error.set(null);

        const params = new HttpParams().set('referencia', referencia);

        try {
            await firstValueFrom(
                this.http.delete(this.baseUrl, { params })
            );
            // Optimistic update
            this._imagenes.update(imgs => imgs.filter(img => img.referencia !== referencia));
        } catch (e) {
            this.handleError(e, 'Error al eliminar la imagen');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }
    
    clearError() {
      this._error.set(null);
    }

    private handleError(e: unknown, fallbackMessage: string) {
        if (e instanceof HttpErrorResponse) {
            const problem = e.error as ProblemDetail | null;
            this._error.set(problem?.detail ?? fallbackMessage);
        } else {
            this._error.set('Error de red o de servidor');
        }
    }
}
