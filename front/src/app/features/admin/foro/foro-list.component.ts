import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminForoService } from '../../../core/services/admin-foro.service';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';

@Component({
  selector: 'app-admin-foro-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ImagenUrlPipe],
  template: `
    <div class="space-y-8">
      <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">Foro de Viajes</h1>
          <p class="text-[var(--color-muted)] mt-1">Publicaciones editoriales y contenido de engagement.</p>
        </div>
        <a routerLink="nuevo" class="px-4 py-2 rounded-xl bg-[var(--color-ink)] text-white font-medium hover:bg-[var(--color-night)] transition-colors inline-flex items-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
          Nuevo post
        </a>
      </div>

      @if (foroService.error()) {
        <div class="bg-red-900/20 border border-red-500/30 rounded-xl p-4">
          <p class="text-sm font-medium text-red-700">{{ foroService.error() }}</p>
        </div>
      }

      @if (foroService.loading() && foroService.posts().length === 0) {
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          @for (i of [1,2,3]; track i) {
            <div class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white/80 animate-pulse overflow-hidden">
              <div class="h-40 bg-[var(--color-sea)]/50"></div>
              <div class="p-4 space-y-3">
                <div class="h-4 w-1/3 rounded bg-[var(--color-sea)]/50"></div>
                <div class="h-6 w-4/5 rounded bg-[var(--color-sea)]/50"></div>
                <div class="h-4 w-full rounded bg-[var(--color-sea)]/50"></div>
              </div>
            </div>
          }
        </div>
      } @else if (foroService.posts().length === 0) {
        <div class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white/85 p-10 text-center">
          <h2 class="text-xl font-bold text-[var(--color-night)]">No hay publicaciones del foro</h2>
          <p class="text-[var(--color-muted)] mt-2">Crea el primer artículo para empezar a generar comunidad.</p>
        </div>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
          @for (post of foroService.posts(); track post.id) {
            <article class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white/90 overflow-hidden flex flex-col">
              <div class="h-44 bg-[var(--color-cloud)]">
                @if (post.imagenPortada) {
                  <img [src]="post.imagenPortada | imagenUrl" [alt]="post.titulo" class="w-full h-full object-cover" loading="lazy" />
                } @else {
                  <div class="w-full h-full flex items-center justify-center text-[var(--color-muted-soft)] text-sm">Sin portada</div>
                }
              </div>
              <div class="p-5 flex flex-col flex-grow">
                <div class="text-xs uppercase tracking-[0.15em] text-[var(--color-muted-soft)] mb-2">/{{ post.slug }}</div>
                <h2 class="text-xl font-bold text-[var(--color-night)] mb-2 line-clamp-2">{{ post.titulo }}</h2>
                <p class="text-sm text-[var(--color-muted)] line-clamp-3 flex-grow">{{ post.resumen }}</p>
                <div class="mt-4 text-xs text-[var(--color-muted-soft)]">{{ post.totalComentarios }} comentarios</div>
                <div class="mt-4 pt-4 border-t border-[var(--color-sea-strong)]/35 flex items-center gap-2">
                  <a [routerLink]="['edit', post.id]" class="px-3 py-2 rounded-lg border border-[var(--color-sea-strong)]/45 text-sm text-[var(--color-night)] hover:bg-[var(--color-sea)]/45 transition-colors">Editar</a>
                  <button type="button" (click)="delete(post.id)" class="px-3 py-2 rounded-lg border border-red-500/35 text-sm text-red-600 hover:bg-red-500/10 transition-colors">Eliminar</button>
                  <a [routerLink]="['/foro', post.slug]" target="_blank" rel="noopener noreferrer" class="ml-auto text-sm font-medium text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors">Ver</a>
                </div>
              </div>
            </article>
          }
        </div>
      }
    </div>
  `,
})
export default class ForoListComponent implements OnInit {
  readonly foroService = inject(AdminForoService);

  async ngOnInit() {
    await this.foroService.loadPosts();
  }

  async delete(id: number) {
    if (!confirm('¿Seguro que quieres eliminar esta publicación del foro?')) {
      return;
    }

    await this.foroService.deletePost(id);
  }
}
