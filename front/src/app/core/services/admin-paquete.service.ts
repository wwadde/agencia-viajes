import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaqueteResumenDTO, PaqueteDetalleDTO, PaqueteUpsertDTO } from '../models/paquete.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminPaqueteService {
    private readonly baseUrl = `${environment.apiUrl}/admin/paquetes`;
    private http = inject(HttpClient);

    private _paquetes = signal<PaqueteResumenDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly paquetes = this._paquetes.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadPaquetes() {
        this._loading.set(true);
        this._error.set(null);
        try {
            const result = await firstValueFrom(this.http.get<PaqueteResumenDTO[]>(this.baseUrl));
            this._paquetes.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar paquetes');
        } finally {
            this._loading.set(false);
        }
    }

    async getPaquete(id: number): Promise<PaqueteDetalleDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<PaqueteDetalleDTO>(`${this.baseUrl}/${id}`));
        } catch (e) {
            this.handleError(e, 'Error al obtener paquete');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    async createPaquete(data: PaqueteUpsertDTO): Promise<PaqueteDetalleDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const paq = await firstValueFrom(this.http.post<PaqueteDetalleDTO>(this.baseUrl, data));
            await this.loadPaquetes();
            return paq;
        } catch (e) {
            this.handleError(e, 'Error al crear paquete');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async updatePaquete(id: number, data: PaqueteUpsertDTO): Promise<PaqueteDetalleDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const paq = await firstValueFrom(this.http.put<PaqueteDetalleDTO>(`${this.baseUrl}/${id}`, data));
            await this.loadPaquetes();
            return paq;
        } catch (e) {
            this.handleError(e, 'Error al actualizar paquete');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async deletePaquete(id: number): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.delete(`${this.baseUrl}/${id}`));
            // Optimistic update
            this._paquetes.update(list => list.filter(p => p.id !== id));
        } catch (e) {
            this.handleError(e, 'Error al eliminar paquete');
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
            this._error.set(this.extractProblemMessage(e, fallbackMessage));
        } else {
            this._error.set('Error de red o de servidor');
        }
    }

    private extractProblemMessage(error: HttpErrorResponse, fallbackMessage: string): string {
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
        return fallbackMessage;
    }
}
