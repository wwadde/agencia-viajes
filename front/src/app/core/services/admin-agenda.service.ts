import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AgendaSalidaDTO, AgendaSalidaUpsertDTO } from '../models/agenda.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminAgendaService {
    private readonly baseUrl = `${environment.apiUrl}/admin/agendas`;
    private http = inject(HttpClient);

    private _agendas = signal<AgendaSalidaDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly agendas = this._agendas.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadAgendas(paqueteId?: number) {
        this._loading.set(true);
        this._error.set(null);
        
        let params = new HttpParams();
        if (paqueteId) {
            params = params.set('paqueteId', paqueteId.toString());
        }

        try {
            const result = await firstValueFrom(this.http.get<AgendaSalidaDTO[]>(this.baseUrl, { params }));
            this._agendas.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar agendas');
        } finally {
            this._loading.set(false);
        }
    }

    async getAgenda(id: number): Promise<AgendaSalidaDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<AgendaSalidaDTO>(`${this.baseUrl}/${id}`));
        } catch (e) {
            this.handleError(e, 'Error al obtener agenda');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    async createAgenda(data: AgendaSalidaUpsertDTO): Promise<AgendaSalidaDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const ag = await firstValueFrom(this.http.post<AgendaSalidaDTO>(this.baseUrl, data));
            await this.loadAgendas(); // reload
            return ag;
        } catch (e) {
            this.handleError(e, 'Error al crear agenda');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async updateAgenda(id: number, data: AgendaSalidaUpsertDTO): Promise<AgendaSalidaDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const ag = await firstValueFrom(this.http.put<AgendaSalidaDTO>(`${this.baseUrl}/${id}`, data));
            await this.loadAgendas();
            return ag;
        } catch (e) {
            this.handleError(e, 'Error al actualizar agenda');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async deleteAgenda(id: number): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.delete(`${this.baseUrl}/${id}`));
            // Optimistic update
            this._agendas.update(list => list.filter(a => a.id !== id));
        } catch (e) {
            this.handleError(e, 'Error al eliminar agenda');
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
