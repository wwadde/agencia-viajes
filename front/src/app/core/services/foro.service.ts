import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ForoComentarioCrearDTO,
  ForoComentarioDTO,
  ForoPostPageDTO,
  ForoPostDetalleDTO,
  ForoReaccionDTO,
  ForoPostResumenDTO,
} from '../models/foro.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class ForoService {
  private readonly baseUrl = `${environment.apiUrl}/foro/posts`;
  private readonly http = inject(HttpClient);

  private _posts = signal<ForoPostResumenDTO[]>([]);
  private _loading = signal(false);
  private _error = signal<string | null>(null);
  private _comentando = signal(false);
  private _reactingPost = signal(false);
  private _reactingComment = signal<number | null>(null);
  private _page = signal(0);
  private _size = signal(9);
  private _totalPages = signal(0);
  private _totalItems = signal(0);
  private _query = signal('');

  readonly posts = this._posts.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly comentando = this._comentando.asReadonly();
  readonly reactingPost = this._reactingPost.asReadonly();
  readonly reactingComment = this._reactingComment.asReadonly();
  readonly page = this._page.asReadonly();
  readonly size = this._size.asReadonly();
  readonly totalPages = this._totalPages.asReadonly();
  readonly totalItems = this._totalItems.asReadonly();
  readonly query = this._query.asReadonly();

  async loadPosts(options?: { query?: string; page?: number; size?: number }): Promise<void> {
    this._loading.set(true);
    this._error.set(null);

    const page = options?.page ?? this._page();
    const size = options?.size ?? this._size();
    const query = options?.query ?? this._query();

    let params = new HttpParams()
      .set('page', String(Math.max(page, 0)))
      .set('size', String(Math.max(size, 1)));
    if (query?.trim()) {
      params = params.set('q', query.trim());
    }

    try {
      const data = await firstValueFrom(this.http.get<ForoPostPageDTO>(this.baseUrl, { params }));
      this._posts.set(data.items || []);
      this._page.set(data.page ?? page);
      this._size.set(data.size ?? size);
      this._totalPages.set(data.totalPages ?? 0);
      this._totalItems.set(data.totalItems ?? 0);
      this._query.set(query ?? '');
    } catch (e) {
      this.handleError(e, 'No se pudieron cargar las publicaciones del foro.');
      this._posts.set([]);
      this._totalPages.set(0);
      this._totalItems.set(0);
    } finally {
      this._loading.set(false);
    }
  }

  async getPost(slug: string): Promise<ForoPostDetalleDTO | null> {
    this._loading.set(true);
    this._error.set(null);
    try {
      return await firstValueFrom(this.http.get<ForoPostDetalleDTO>(`${this.baseUrl}/${slug}`));
    } catch (e) {
      this.handleError(e, 'No se pudo cargar la publicación.');
      return null;
    } finally {
      this._loading.set(false);
    }
  }

  async comentar(slug: string, dto: ForoComentarioCrearDTO): Promise<ForoComentarioDTO | null> {
    this._comentando.set(true);
    this._error.set(null);
    try {
      return await firstValueFrom(this.http.post<ForoComentarioDTO>(`${this.baseUrl}/${slug}/comentarios`, dto));
    } catch (e) {
      this.handleError(e, 'No se pudo registrar tu comentario.');
      return null;
    } finally {
      this._comentando.set(false);
    }
  }

  async toggleLikePost(slug: string): Promise<ForoReaccionDTO | null> {
    this._reactingPost.set(true);
    this._error.set(null);
    try {
      return await firstValueFrom(this.http.post<ForoReaccionDTO>(`${this.baseUrl}/${slug}/likes/toggle`, {}));
    } catch (e) {
      this.handleError(e, 'No se pudo registrar tu reacción.');
      return null;
    } finally {
      this._reactingPost.set(false);
    }
  }

  async toggleLikeComentario(slug: string, comentarioId: number): Promise<ForoReaccionDTO | null> {
    this._reactingComment.set(comentarioId);
    this._error.set(null);
    try {
      return await firstValueFrom(this.http.post<ForoReaccionDTO>(`${this.baseUrl}/${slug}/comentarios/${comentarioId}/likes/toggle`, {}));
    } catch (e) {
      this.handleError(e, 'No se pudo registrar tu reacción.');
      return null;
    } finally {
      this._reactingComment.set(null);
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
      this._error.set('Error de red o de servidor.');
    }
  }
}
