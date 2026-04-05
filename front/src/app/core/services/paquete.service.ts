import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PaqueteResumenDTO, PaqueteDetalleDTO } from '../models/paquete.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class PaqueteService {
    private readonly baseUrl = `${environment.apiUrl}/paquetes`;
    private http = inject(HttpClient);

    // Private writable signals
    private _paquetes = signal<PaqueteResumenDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    // Public readonly signals
    readonly paquetes = this._paquetes.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    /**
     * Carga el listado de paquetes desde el backend
     */
    async loadPaquetes(): Promise<void> {
        this._loading.set(true);
        this._error.set(null);
        try {
            const paquetes = await firstValueFrom(
                this.http.get<PaqueteResumenDTO[]>(this.baseUrl)
            );
            this._paquetes.set(paquetes || []);
        } catch (e) {
            if (e instanceof HttpErrorResponse) {
                const problem = e.error as ProblemDetail | null;
                this._error.set(problem?.detail ?? 'No se pudieron cargar los paquetes.');
            } else {
                this._error.set('Error de conexión. Inténtalo de nuevo más tarde.');
            }
            this._paquetes.set([]);
        } finally {
            this._loading.set(false);
        }
    }

    /**
     * Obtiene el detalle de un paquete (por ID o slug)
     */
    async getPaqueteDetalle(slugOrId: string | number): Promise<PaqueteDetalleDTO | null> {
        this._loading.set(true);
        this._error.set(null);
        try {
            return await firstValueFrom(
                this.http.get<PaqueteDetalleDTO>(`${this.baseUrl}/${slugOrId}`)
            );
        } catch (e) {
            this._error.set('No se pudo encontrar el paquete.');
            return null;
        } finally {
            this._loading.set(false);
        }
    }
}
