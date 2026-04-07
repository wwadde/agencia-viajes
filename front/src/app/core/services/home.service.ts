import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HomeDataDTO, HomeLandingSlideDTO } from '../models/home.model';
import { PaqueteResumenDTO } from '../models/paquete.model';
import { ForoPostPageDTO, ForoPostResumenDTO } from '../models/foro.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class HomeService {
    private readonly baseUrl = `${environment.apiUrl}/home`;
    private readonly foroBaseUrl = `${environment.apiUrl}/foro/posts`;
    private http = inject(HttpClient);

    private _paquetes = signal<PaqueteResumenDTO[]>([]);
    private _foroPosts = signal<ForoPostResumenDTO[]>([]);
    private _landingSlides = signal<HomeLandingSlideDTO[]>([]);
    private _loading = signal(false);
    private _error = signal<string | null>(null);

    readonly paquetes = this._paquetes.asReadonly();
    readonly foroPosts = this._foroPosts.asReadonly();
    readonly landingSlides = this._landingSlides.asReadonly();
    readonly loading = this._loading.asReadonly();
    readonly error = this._error.asReadonly();

    async loadHomeData(): Promise<void> {
        this._loading.set(true);
        this._error.set(null);

        try {
            const foroParams = new HttpParams()
                .set('page', '0')
                .set('size', '3');

            const [data, foroData] = await Promise.all([
                firstValueFrom(this.http.get<HomeDataDTO>(this.baseUrl)),
                firstValueFrom(this.http.get<ForoPostPageDTO>(this.foroBaseUrl, { params: foroParams })).catch(() => null),
            ]);

            this._paquetes.set(data?.paquetes ?? []);
            this._landingSlides.set(data?.landingSlides ?? []);
            this._foroPosts.set(foroData?.items ?? []);
        } catch (e) {
            if (e instanceof HttpErrorResponse) {
                const problem = e.error as ProblemDetail | null;
                this._error.set(problem?.detail ?? 'No se pudo cargar la página principal.');
            } else {
                this._error.set('Error de conexión. Inténtalo de nuevo más tarde.');
            }

            this._paquetes.set([]);
            this._foroPosts.set([]);
            this._landingSlides.set([]);
        } finally {
            this._loading.set(false);
        }
    }

    clearError() {
        this._error.set(null);
    }
}
