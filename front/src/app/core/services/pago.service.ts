import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagoCrearIntentoDTO, PagoIntentoDTO, PagoResumenDTO } from '../models/pago.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class PagoService {
    private readonly baseUrl = environment.apiUrl;
    private http = inject(HttpClient);

    // Private writable signals
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    // Public read-only signals
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    /**
     * Crea un intento de pago PayU para una reserva.
     */
    async crearIntento(reservaId: number, metadata?: PagoCrearIntentoDTO): Promise<PagoIntentoDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const url = `${this.baseUrl}/reservas/${reservaId}/pagos/intentos`;
            return await firstValueFrom(
                this.http.post<PagoIntentoDTO>(url, metadata || {})
            );
        } catch (e) {
            this.handleError(e, 'Error al crear el intento de pago');
            return null;
        } finally {
            this._loading.set(false);
        }
    }

    /**
     * Obtiene el historial de pagos (intentos/transacciones) de una reserva.
     */
    async obtenerPagosReserva(reservaId: number): Promise<PagoResumenDTO[]> {
        // No asertamos error aquí para permitir polling de fondo sin corromper UI general bruscamente
        try {
            return await firstValueFrom(
                this.http.get<PagoResumenDTO[]>(`${this.baseUrl}/reservas/${reservaId}/pagos`)
            );
        } catch (e) {
            // Lógica de error silenciosa (solo consola) útil para polling
            console.error('Error obteniendo historial de pagos', e);
            return [];
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
            this._error.set('Error de red o de servidor general');
        }
    }
}
