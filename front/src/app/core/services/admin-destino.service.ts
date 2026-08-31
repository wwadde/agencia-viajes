import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DestinoDTO, DestinoUpsertDTO } from '../models/destino.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminDestinoService {
    private readonly baseUrl = `${environment.apiUrl}/admin/destinos`;
    private http = inject(HttpClient);

    private _destinos = signal<DestinoDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly destinos = this._destinos.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadDestinos() {
        this._loading.set(true);
        this._error.set(null);
        try {
            const result = await firstValueFrom(this.http.get<DestinoDTO[]>(this.baseUrl));
            this._destinos.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar destinos');
        } finally {
            this._loading.set(false);
        }
    }

    async getDestino(id: number): Promise<DestinoDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<DestinoDTO>(`${this.baseUrl}/${id}`));
        } catch (e) {
            this.handleError(e, 'Error al obtener destino');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    async createDestino(data: DestinoUpsertDTO): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.post<void>(this.baseUrl, data));
            await this.loadDestinos(); // recargar
        } catch (e) {
            this.handleError(e, 'Error al crear destino');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async updateDestino(id: number, data: DestinoUpsertDTO): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.put<void>(`${this.baseUrl}/${id}`, data));
            await this.loadDestinos(); // recargar
        } catch (e) {
            this.handleError(e, 'Error al actualizar destino');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async deleteDestino(id: number): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.delete(`${this.baseUrl}/${id}`));
            // Optimistic update
            this._destinos.update(list => list.filter(d => d.id !== id));
        } catch (e) {
            this.handleError(e, 'Error al eliminar destino');
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
