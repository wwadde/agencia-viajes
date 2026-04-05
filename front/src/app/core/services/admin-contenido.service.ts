import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ContenidoDTO, ContenidoUpsertDTO } from '../models/contenido.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminContenidoService {
    private readonly baseUrl = `${environment.apiUrl}/admin/contenidos`;
    private http = inject(HttpClient);

    private _contenidos = signal<ContenidoDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly contenidos = this._contenidos.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadContenidos() {
        this._loading.set(true);
        this._error.set(null);
        try {
            const result = await firstValueFrom(this.http.get<ContenidoDTO[]>(this.baseUrl));
            this._contenidos.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar contenidos dinámicos');
        } finally {
            this._loading.set(false);
        }
    }

    async getContenido(id: number): Promise<ContenidoDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<ContenidoDTO>(`${this.baseUrl}/${id}`));
        } catch (e) {
            this.handleError(e, 'Error al obtener contenido');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    async createContenido(data: ContenidoUpsertDTO): Promise<ContenidoDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const cont = await firstValueFrom(this.http.post<ContenidoDTO>(this.baseUrl, data));
            await this.loadContenidos();
            return cont;
        } catch (e) {
            this.handleError(e, 'Error al crear contenido');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async updateContenido(id: number, data: ContenidoUpsertDTO): Promise<ContenidoDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const cont = await firstValueFrom(this.http.put<ContenidoDTO>(`${this.baseUrl}/${id}`, data));
            await this.loadContenidos();
            return cont;
        } catch (e) {
            this.handleError(e, 'Error al actualizar contenido');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async deleteContenido(id: number): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.delete(`${this.baseUrl}/${id}`));
            // Optimistic update
            this._contenidos.update(list => list.filter(c => c.id !== id));
        } catch (e) {
            this.handleError(e, 'Error al eliminar contenido');
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
