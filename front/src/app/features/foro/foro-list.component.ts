import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForoService } from '../../core/services/foro.service';
import { AuthService } from '../../core/services/auth.service';
import { ImagenUrlPipe } from '../../core/pipes/imagen-url.pipe';
import { SeoService } from '../../core/services/seo.service';

@Component({
  selector: 'app-foro-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ImagenUrlPipe, DatePipe, FormsModule],
  template: `
    <div class="theme-shell min-h-screen text-[var(--color-ink)] flex flex-col">
      <header class="border-b border-white/40 bg-[var(--color-cloud)]/80 backdrop-blur-xl sticky top-0 z-40">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
          <a routerLink="/" class="inline-flex items-center gap-2 text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Inicio
          </a>
          <div class="text-sm text-[var(--color-muted)]">
            @if (auth.isAuthenticated()) {
              <span class="font-medium">Hola {{ auth.user()?.usuario }}</span>
            } @else {
              <a routerLink="/login" class="font-medium hover:text-[var(--color-ink)] transition-colors">Inicia sesión para comentar</a>
            }
          </div>
        </div>
      </header>

      <main class="max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-10 sm:py-14 flex-grow">
        <section class="rounded-[2rem] border border-white/65 bg-[linear-gradient(135deg,rgba(40,53,57,0.95),rgba(31,42,45,0.9))] text-white p-8 sm:p-12 shadow-2xl mb-12">
          <p class="text-xs font-semibold tracking-[0.2em] uppercase text-[var(--color-sea)] mb-4">Foro Viajero</p>
          <h1 class="text-4xl sm:text-5xl lg:text-6xl font-extrabold leading-[1.02] tracking-tight max-w-3xl">Historias reales, consejos prácticos y destinos que enamoran.</h1>
          <p class="text-white/80 mt-6 text-lg max-w-2xl leading-relaxed">Explora publicaciones visuales de la comunidad de viajeros, encuentra inspiración y comparte tu opinión en cada artículo.</p>
        </section>

        <section class="mb-8 rounded-2xl border border-white/70 bg-white/80 p-4 sm:p-5">
          <form (ngSubmit)="search()" class="flex flex-col sm:flex-row gap-3">
            <input
              type="text"
              [(ngModel)]="queryInput"
              name="query"
              placeholder="Buscar por destino, consejo o tema..."
              class="flex-1 rounded-xl border border-[var(--color-sea-strong)]/35 bg-white px-4 py-2.5 text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/20"
            />
            <div class="flex gap-2">
              <button type="submit" class="px-4 py-2.5 rounded-xl bg-[var(--color-ink)] text-white font-medium hover:bg-[var(--color-night)] transition-colors">Buscar</button>
              <button type="button" (click)="clearSearch()" class="px-4 py-2.5 rounded-xl border border-[var(--color-sea-strong)]/45 text-[var(--color-muted)] hover:bg-[var(--color-sea)]/35 transition-colors">Limpiar</button>
            </div>
          </form>
          <p class="text-xs text-[var(--color-muted-soft)] mt-3">{{ foroService.totalItems() }} publicaciones encontradas.</p>
        </section>

        @if (foroService.loading()) {
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
            @for (i of [1,2,3,4,5,6]; track i) {
              <article class="rounded-3xl border border-white/70 bg-white/70 overflow-hidden animate-pulse">
                <div class="h-56 bg-[var(--color-sea)]/50"></div>
                <div class="p-6 space-y-3">
                  <div class="h-4 w-1/4 bg-[var(--color-sea)]/50 rounded"></div>
                  <div class="h-7 w-4/5 bg-[var(--color-sea)]/50 rounded"></div>
                  <div class="h-4 w-full bg-[var(--color-sea)]/50 rounded"></div>
                  <div class="h-4 w-3/4 bg-[var(--color-sea)]/50 rounded"></div>
                </div>
              </article>
            }
          </div>
        } @else if (foroService.error()) {
          <div class="max-w-2xl mx-auto rounded-2xl border border-red-500/40 bg-red-900/15 p-8 text-center">
            <h2 class="text-2xl font-bold text-[var(--color-night)]">No pudimos cargar el foro</h2>
            <p class="text-[var(--color-muted)] mt-2">{{ foroService.error() }}</p>
            <button (click)="reload()" class="mt-5 px-5 py-2.5 rounded-xl bg-[var(--color-ink)] hover:bg-[var(--color-night)] text-white font-medium transition-colors">Reintentar</button>
          </div>
        } @else if (foroService.posts().length === 0) {
          <div class="max-w-2xl mx-auto rounded-2xl border border-white/70 bg-white/80 p-10 text-center">
            <h2 class="text-2xl font-bold text-[var(--color-night)]">Aún no hay publicaciones</h2>
            <p class="text-[var(--color-muted)] mt-2">Estamos preparando contenido nuevo para el foro. Vuelve pronto.</p>
          </div>
        } @else {
          <section class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
            @for (post of foroService.posts(); track post.id) {
              <article class="group rounded-3xl border border-white/75 bg-white/75 overflow-hidden shadow-xl hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 flex flex-col">
                <a [routerLink]="['/foro', post.slug]" [attr.aria-label]="'Leer publicación ' + post.titulo" class="h-56 bg-[var(--color-cloud)] relative overflow-hidden block">
                  @if (post.imagenPortada) {
                    <img [src]="post.imagenPortada | imagenUrl" [alt]="post.titulo" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" loading="lazy" />
                  } @else {
                    <div class="w-full h-full flex items-center justify-center text-[var(--color-muted-soft)]">
                      <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14" />
                      </svg>
                    </div>
                  }
                  <div class="absolute top-4 left-4 inline-flex items-center px-3 py-1 rounded-full bg-[var(--color-night)]/80 text-white text-xs font-semibold tracking-wider uppercase backdrop-blur-sm">
                    Foro
                  </div>
                </a>

                <div class="p-6 flex flex-col flex-grow">
                  <div class="text-xs uppercase tracking-[0.15em] font-semibold text-[var(--color-muted-soft)] mb-3">
                    {{ post.fechaPublicacion | date:'dd MMM yyyy' }} • {{ post.totalComentarios }} comentarios
                  </div>
                  <h2 class="text-2xl font-bold leading-tight text-[var(--color-night)] mb-3">{{ post.titulo }}</h2>
                  <p class="text-[var(--color-muted)] leading-relaxed line-clamp-3 flex-grow">{{ post.resumen }}</p>

                  <div class="mt-4 text-sm text-[var(--color-muted)] inline-flex items-center gap-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12.1 21.35l-1.1-1.02C5.14 15.24 2 12.39 2 8.9 2 6.05 4.24 3.8 7.1 3.8c1.62 0 3.17.76 4.2 1.95 1.03-1.19 2.58-1.95 4.2-1.95 2.86 0 5.1 2.25 5.1 5.1 0 3.49-3.14 6.34-8.9 11.43l-1.6 1.02z"/></svg>
                    <span>{{ post.totalLikes }} me gusta</span>
                  </div>

                  <a [routerLink]="['/foro', post.slug]" class="mt-6 inline-flex items-center gap-2 font-semibold text-[var(--color-ink)] hover:text-[var(--color-night)] transition-colors">
                    Leer publicación
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                  </a>
                </div>
              </article>
            }
          </section>

          @if (foroService.totalPages() > 1) {
            <div class="mt-10 flex flex-wrap items-center justify-center gap-3">
              <button
                type="button"
                (click)="goToPage(foroService.page() - 1)"
                [disabled]="!canGoPrev()"
                class="px-4 py-2 rounded-xl border border-[var(--color-sea-strong)]/45 bg-white/80 text-[var(--color-ink)] disabled:opacity-45 disabled:cursor-not-allowed"
              >Anterior</button>
              <span class="text-sm text-[var(--color-muted)]">Página {{ foroService.page() + 1 }} de {{ foroService.totalPages() }}</span>
              <button
                type="button"
                (click)="goToPage(foroService.page() + 1)"
                [disabled]="!canGoNext()"
                class="px-4 py-2 rounded-xl border border-[var(--color-sea-strong)]/45 bg-white/80 text-[var(--color-ink)] disabled:opacity-45 disabled:cursor-not-allowed"
              >Siguiente</button>
            </div>
          }
        }
      </main>
    </div>
  `,
})
export default class ForoListComponent implements OnInit {
  readonly foroService = inject(ForoService);
  readonly auth = inject(AuthService);
  private readonly seo = inject(SeoService);
  queryInput = '';

  async ngOnInit() {
    this.seo.setForumListingSeo();
    await Promise.all([
      this.foroService.loadPosts({ page: 0, size: 9 }),
      this.auth.checkAuthIfSessionLikelyActive(),
    ]);
    this.queryInput = this.foroService.query();
  }

  async reload() {
    await this.foroService.loadPosts({ query: this.foroService.query(), page: this.foroService.page(), size: this.foroService.size() });
  }

  async search() {
    await this.foroService.loadPosts({ query: this.queryInput, page: 0, size: this.foroService.size() });
  }

  async clearSearch() {
    this.queryInput = '';
    await this.foroService.loadPosts({ query: '', page: 0, size: this.foroService.size() });
  }

  async goToPage(page: number) {
    await this.foroService.loadPosts({ query: this.foroService.query(), page, size: this.foroService.size() });
  }

  canGoPrev(): boolean {
    return this.foroService.page() > 0;
  }

  canGoNext(): boolean {
    return this.foroService.page() + 1 < this.foroService.totalPages();
  }
}
