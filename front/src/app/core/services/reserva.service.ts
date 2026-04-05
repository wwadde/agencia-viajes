import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReservaCrearDTO } from '../models/reserva.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class ReservaService {
    private readonly baseUrl = `${environment.apiUrl}/reservas`;
    private http = inject(HttpClient);

    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async crearReserva(data: ReservaCrearDTO): Promise<boolean> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const response = await firstValueFrom(
                this.http.post<void>(this.baseUrl, data, { observe: 'response' })
            );
            return response.status >= 200 && response.status < 300;
        } catch (e) {
            this.handleError(e, 'Error al crear la reserva');
            return false;
        } finally {
            this._loading.set(false);
        }
    }

    async cancelarReserva(id: number): Promise<boolean> {
        this._loading.set(true);
        this._error.set(null);
        try {
            await firstValueFrom(this.http.delete(`${this.baseUrl}/${id}`));
            return true;
        } catch (e) {
            this.handleError(e, 'Error al cancelar la reserva');
            return false;
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
            this._error.set('Error general. Revisa tu conexión.');
        }
    }
}
