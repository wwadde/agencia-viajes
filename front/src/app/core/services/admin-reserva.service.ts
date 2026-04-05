import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReservaDTO, ReservaEstadoUpdateDTO, ReservaEstado } from '../models/reserva.model';
import { PagoResumenDTO } from '../models/pago.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminReservaService {
    private readonly baseUrl = `${environment.apiUrl}/admin/reservas`;
    private http = inject(HttpClient);

    private _reservas = signal<ReservaDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly reservas = this._reservas.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadReservas(estado?: ReservaEstado) {
        this._loading.set(true);
        this._error.set(null);
        
        let params = new HttpParams();
        if (estado) {
            params = params.set('estado', estado);
        }

        try {
            const result = await firstValueFrom(this.http.get<ReservaDTO[]>(this.baseUrl, { params }));
            this._reservas.set(result || []);
        } catch (e) {
            this.handleError(e, 'Error al cargar reservas');
        } finally {
            this._loading.set(false);
        }
    }

    async getReserva(id: number): Promise<ReservaDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<ReservaDTO>(`${this.baseUrl}/${id}`));
        } catch (e) {
            this.handleError(e, 'Error al obtener reserva');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    async updateReservaEstado(id: number, estado: ReservaEstado): Promise<ReservaDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        const data: ReservaEstadoUpdateDTO = { estado };
        try {
            const res = await firstValueFrom(this.http.patch<ReservaDTO>(`${this.baseUrl}/${id}/estado`, data));
            // Optimistic update
            this._reservas.update(list => list.map(r => r.id === id ? { ...r, estado: res.estado } : r));
            return res;
        } catch (e) {
            this.handleError(e, 'Error al actualizar estado de la reserva');
            throw e;
        } finally {
            this._loading.set(false);
        }
    }

    async getReservaPagos(id: number): Promise<PagoResumenDTO[]> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(this.http.get<PagoResumenDTO[]>(`${this.baseUrl}/${id}/pagos`));
        } catch (e) {
            this.handleError(e, 'Error al obtener pagos de la reserva');
            return [];
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
