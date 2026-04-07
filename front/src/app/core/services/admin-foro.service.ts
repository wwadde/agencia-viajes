import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ForoComentarioModeracionDTO, ForoPostDetalleDTO, ForoPostResumenDTO, ForoPostUpsertDTO } from '../models/foro.model';
import type { ProblemDetail } from '../models/error.model';

@Injectable({ providedIn: 'root' })
export class AdminForoService {
  private readonly baseUrl = `${environment.apiUrl}/admin/foro/posts`;
  private readonly http = inject(HttpClient);

  private _posts = signal<ForoPostResumenDTO[]>([]);
  private _loading = signal(false);
  private _error = signal<string | null>(null);

  readonly posts = this._posts.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  async loadPosts(): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      const result = await firstValueFrom(this.http.get<ForoPostResumenDTO[]>(this.baseUrl));
      this._posts.set(result || []);
    } catch (e) {
      this.handleError(e, 'Error al cargar las publicaciones.');
      this._posts.set([]);
    } finally {
      this._loading.set(false);
    }
  }

  async getPost(id: number): Promise<ForoPostDetalleDTO | null> {
    this._loading.set(true);
    this._error.set(null);
    try {
      return await firstValueFrom(this.http.get<ForoPostDetalleDTO>(`${this.baseUrl}/${id}`));
    } catch (e) {
      this.handleError(e, 'Error al obtener la publicación.');
      return null;
    } finally {
      this._loading.set(false);
    }
  }

  async createPost(data: ForoPostUpsertDTO): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      await firstValueFrom(this.http.post<void>(this.baseUrl, data));
      await this.loadPosts();
    } catch (e) {
      this.handleError(e, 'Error al crear la publicación.');
      throw e;
    } finally {
      this._loading.set(false);
    }
  }

  async updatePost(id: number, data: ForoPostUpsertDTO): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      await firstValueFrom(this.http.put<void>(`${this.baseUrl}/${id}`, data));
      await this.loadPosts();
    } catch (e) {
      this.handleError(e, 'Error al actualizar la publicación.');
      throw e;
    } finally {
      this._loading.set(false);
    }
  }

  async deletePost(id: number): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      await firstValueFrom(this.http.delete<void>(`${this.baseUrl}/${id}`));
      this._posts.update((items) => items.filter((item) => item.id !== id));
    } catch (e) {
      this.handleError(e, 'Error al eliminar la publicación.');
      throw e;
    } finally {
      this._loading.set(false);
    }
  }

  async setComentarioOculto(comentarioId: number, oculto: boolean): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      const body: ForoComentarioModeracionDTO = { oculto };
      await firstValueFrom(this.http.patch<void>(`${this.baseUrl}/comentarios/${comentarioId}/oculto`, body));
    } catch (e) {
      this.handleError(e, 'Error al actualizar la visibilidad del comentario.');
      throw e;
    } finally {
      this._loading.set(false);
    }
  }

  async deleteComentario(comentarioId: number): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    try {
      await firstValueFrom(this.http.delete<void>(`${this.baseUrl}/comentarios/${comentarioId}`));
    } catch (e) {
      this.handleError(e, 'Error al eliminar el comentario.');
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
      this._error.set('Error de red o de servidor.');
    }
  }
}
