import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ForoService } from '../../core/services/foro.service';
import { AuthService } from '../../core/services/auth.service';
import { SeoService } from '../../core/services/seo.service';
import { ForoComentarioDTO, ForoPostDetalleDTO } from '../../core/models/foro.model';
import { ImagenUrlPipe } from '../../core/pipes/imagen-url.pipe';

@Component({
  selector: 'app-foro-detalle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, DatePipe, ImagenUrlPipe],
  template: `
    <div class="theme-shell min-h-screen text-[var(--color-ink)] flex flex-col">
      <header class="border-b border-white/40 bg-[var(--color-cloud)]/80 backdrop-blur-xl sticky top-0 z-40">
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <a routerLink="/foro" class="inline-flex items-center gap-2 text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Volver al foro
          </a>
          <a routerLink="/" class="text-sm font-medium text-[var(--color-muted)] hover:text-[var(--color-ink)] transition-colors">Inicio</a>
        </div>
      </header>

      <main class="max-w-5xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-10 sm:py-14 flex-grow">
        @if (loading()) {
          <div class="min-h-[45vh] flex items-center justify-center">
            <div class="w-12 h-12 rounded-full border-4 border-[var(--color-sea-strong)]/45 border-t-[var(--color-ink)] animate-spin"></div>
          </div>
        } @else if (!post()) {
          <div class="max-w-2xl mx-auto rounded-2xl border border-white/70 bg-white/80 p-10 text-center">
            <h1 class="text-3xl font-extrabold text-[var(--color-night)]">Publicación no encontrada</h1>
            <p class="text-[var(--color-muted)] mt-3">No fue posible cargar este artículo del foro.</p>
            <a routerLink="/foro" class="inline-flex mt-6 px-5 py-2.5 rounded-xl bg-[var(--color-ink)] text-white font-medium hover:bg-[var(--color-night)] transition-colors">Ver otras publicaciones</a>
          </div>
        } @else {
          @let data = post()!;
          <article class="rounded-[2rem] border border-white/70 bg-white/80 overflow-hidden shadow-xl">
            <header class="p-8 sm:p-10 border-b border-[var(--color-sea-strong)]/35 bg-[linear-gradient(145deg,rgba(218,226,223,0.65),rgba(226,218,221,0.58))]">
              <p class="text-xs uppercase tracking-[0.2em] font-semibold text-[var(--color-muted)]">Foro Viajero</p>
              <h1 class="mt-4 text-3xl sm:text-4xl lg:text-5xl font-black text-[var(--color-night)] leading-[1.05]">{{ data.titulo }}</h1>
              <p class="mt-5 text-lg text-[var(--color-muted)] leading-relaxed">{{ data.resumen }}</p>
              <div class="mt-6 text-sm text-[var(--color-muted-soft)] flex flex-wrap gap-x-4 gap-y-1">
                <span>Publicado: {{ data.fechaPublicacion | date:'dd MMM yyyy, HH:mm' }}</span>
                <span>Autor: {{ data.autor || 'Equipo editorial' }}</span>
                <span>{{ comentarios().length }} comentarios</span>
              </div>
              <button
                type="button"
                (click)="toggleLikePost()"
                [disabled]="foroService.reactingPost()"
                class="mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-xl border transition-colors disabled:opacity-60"
                [class]="data.likedByMe
                  ? 'mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-xl border transition-colors disabled:opacity-60 border-[var(--color-ink)] bg-[var(--color-ink)] text-white'
                  : 'mt-5 inline-flex items-center gap-2 px-4 py-2 rounded-xl border transition-colors disabled:opacity-60 border-[var(--color-ink)]/30 text-[var(--color-ink)]'"
              >
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12.1 21.35l-1.1-1.02C5.14 15.24 2 12.39 2 8.9 2 6.05 4.24 3.8 7.1 3.8c1.62 0 3.17.76 4.2 1.95 1.03-1.19 2.58-1.95 4.2-1.95 2.86 0 5.1 2.25 5.1 5.1 0 3.49-3.14 6.34-8.9 11.43l-1.6 1.02z"/></svg>
                <span>{{ data.totalLikes }} me gusta</span>
              </button>
            </header>

            <section class="p-8 sm:p-10 space-y-8">
              @for (bloque of data.bloques; track bloque.id + '-' + $index) {
                @if (bloque.tipo === 'TEXTO') {
                  <div class="prose prose-lg max-w-none prose-p:text-[var(--color-muted)] prose-p:leading-relaxed">
                    <p class="whitespace-pre-wrap">{{ bloque.contenido }}</p>
                  </div>
                } @else {
                  <figure class="rounded-3xl overflow-hidden border border-[var(--color-sea-strong)]/35 bg-[var(--color-cloud)]">
                    <img [src]="bloque.contenido | imagenUrl" [alt]="bloque.pieImagen || data.titulo" loading="lazy" class="w-full h-auto max-h-[34rem] object-cover" />
                    @if (bloque.pieImagen) {
                      <figcaption class="px-5 py-3 text-sm text-[var(--color-muted)] border-t border-[var(--color-sea-strong)]/35">{{ bloque.pieImagen }}</figcaption>
                    }
                  </figure>
                }
              }
            </section>
          </article>

          <section class="mt-10 rounded-[2rem] border border-white/70 bg-white/85 p-8 sm:p-10 shadow-xl">
            <h2 class="text-2xl font-extrabold text-[var(--color-night)] mb-6">Comentarios</h2>

            @if (foroService.error()) {
              <div class="mb-5 rounded-xl border border-red-500/35 bg-red-900/15 p-4 text-sm text-red-700">
                {{ foroService.error() }}
              </div>
            }

            @if (canComment()) {
              <form [formGroup]="commentForm" (ngSubmit)="enviarComentario()" class="mb-8">
                <label for="comment-content" class="block text-sm font-semibold text-[var(--color-muted)] mb-2">Comparte tu opinión</label>
                <textarea
                  id="comment-content"
                  formControlName="contenido"
                  rows="4"
                  class="w-full rounded-2xl border border-[var(--color-sea-strong)]/40 bg-white px-4 py-3 text-[var(--color-ink)] focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/25"
                  placeholder="Escribe tu comentario sobre este artículo..."
                ></textarea>
                <div class="mt-3 flex items-center justify-between gap-4">
                  <p class="text-xs text-[var(--color-muted-soft)]">Máximo 1500 caracteres.</p>
                  <button
                    type="submit"
                    [disabled]="commentForm.invalid || foroService.comentando()"
                    class="px-5 py-2.5 rounded-xl bg-[var(--color-ink)] text-white font-semibold hover:bg-[var(--color-night)] disabled:bg-[var(--color-sea-strong)] disabled:text-[var(--color-muted)] transition-colors"
                  >
                    {{ foroService.comentando() ? 'Enviando...' : 'Publicar comentario' }}
                  </button>
                </div>
              </form>
            } @else {
              <div class="mb-8 rounded-2xl border border-[var(--color-sea-strong)]/45 bg-[var(--color-sea)]/35 p-5 text-sm text-[var(--color-muted)]">
                @if (!auth.isAuthenticated()) {
                  <p>
                    Inicia sesión como cliente para comentar.
                    <button type="button" (click)="goToLogin()" class="ml-1 font-semibold text-[var(--color-ink)] hover:text-[var(--color-night)]">Ir a login</button>
                  </p>
                } @else {
                  <p>Solo los usuarios con perfil de cliente pueden comentar en las publicaciones del foro.</p>
                }
              </div>
            }

            @if (comentarios().length === 0) {
              <p class="text-[var(--color-muted)]">Todavía no hay comentarios. Sé el primero en participar.</p>
            } @else {
              <div class="space-y-4">
                @for (comentario of comentarios(); track comentario.id) {
                  <article class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white p-4">
                    <div class="flex items-center justify-between gap-4 text-sm mb-2">
                      <strong class="text-[var(--color-night)]">{{ comentario.autor }}</strong>
                      <span class="text-[var(--color-muted-soft)]">{{ comentario.fechaCreacion | date:'dd MMM yyyy, HH:mm' }}</span>
                    </div>
                    <p class="text-[var(--color-muted)] whitespace-pre-wrap leading-relaxed">{{ comentario.contenido }}</p>
                    <div class="mt-3">
                      <button
                        type="button"
                        (click)="toggleLikeComentario(comentario.id)"
                        [disabled]="foroService.reactingComment() === comentario.id"
                        class="inline-flex items-center gap-2 text-sm px-3 py-1.5 rounded-lg border disabled:opacity-60"
                        [class]="comentario.likedByMe
                          ? 'inline-flex items-center gap-2 text-sm px-3 py-1.5 rounded-lg border disabled:opacity-60 border-[var(--color-ink)] bg-[var(--color-ink)] text-white'
                          : 'inline-flex items-center gap-2 text-sm px-3 py-1.5 rounded-lg border disabled:opacity-60 border-[var(--color-sea-strong)]/45 text-[var(--color-muted)]'"
                      >
                        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12.1 21.35l-1.1-1.02C5.14 15.24 2 12.39 2 8.9 2 6.05 4.24 3.8 7.1 3.8c1.62 0 3.17.76 4.2 1.95 1.03-1.19 2.58-1.95 4.2-1.95 2.86 0 5.1 2.25 5.1 5.1 0 3.49-3.14 6.34-8.9 11.43l-1.6 1.02z"/></svg>
                        <span>{{ comentario.totalLikes }} me gusta</span>
                      </button>
                    </div>
                  </article>
                }
              </div>
            }
          </section>
        }
      </main>
    </div>
  `,
})
export default class ForoDetalleComponent implements OnInit {
  readonly foroService = inject(ForoService);
  readonly auth = inject(AuthService);
  private readonly seo = inject(SeoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly post = signal<ForoPostDetalleDTO | null>(null);
  readonly comentarios = signal<ForoComentarioDTO[]>([]);
  readonly loading = signal(true);
  readonly canComment = computed(() => this.auth.isAuthenticated() && this.auth.roles() === 'CLIENTE');
  readonly canReact = computed(() => this.auth.isAuthenticated() && this.auth.roles() === 'CLIENTE');

  readonly commentForm = this.fb.group({
    contenido: ['', [Validators.required, Validators.maxLength(1500)]],
  });

  async ngOnInit() {
    await this.auth.checkAuthIfSessionLikelyActive();

    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.loading.set(false);
      this.seo.setForumNotFoundSeo();
      return;
    }

    const post = await this.foroService.getPost(slug);
    this.post.set(post);
    this.comentarios.set(post?.comentarios ?? []);

    if (post) {
      this.seo.setForumPostSeo(post, slug);
    } else {
      this.seo.setForumNotFoundSeo();
    }

    this.loading.set(false);
  }

  async enviarComentario() {
    if (this.commentForm.invalid || !this.post()) {
      return;
    }

    const slug = this.post()!.slug;
    const contenido = this.commentForm.controls.contenido.value?.trim() ?? '';
    if (!contenido) {
      return;
    }

    const comentario = await this.foroService.comentar(slug, { contenido });
    if (!comentario) {
      return;
    }

    this.comentarios.update((items) => [...items, comentario]);
    this.commentForm.reset();
  }

  async toggleLikePost() {
    const current = this.post();
    if (!current) {
      return;
    }

    if (!this.canReact()) {
      this.goToLogin();
      return;
    }

    const reaccion = await this.foroService.toggleLikePost(current.slug);
    if (!reaccion) {
      return;
    }

    this.post.set({
      ...current,
      totalLikes: reaccion.totalLikes,
      likedByMe: reaccion.likedByMe,
    });
  }

  async toggleLikeComentario(comentarioId: number) {
    const current = this.post();
    if (!current) {
      return;
    }

    if (!this.canReact()) {
      this.goToLogin();
      return;
    }

    const reaccion = await this.foroService.toggleLikeComentario(current.slug, comentarioId);
    if (!reaccion) {
      return;
    }

    this.comentarios.update((items) => items.map((item) => {
      if (item.id !== comentarioId) {
        return item;
      }

      return {
        ...item,
        totalLikes: reaccion.totalLikes,
        likedByMe: reaccion.likedByMe,
      };
    }));
  }

  goToLogin() {
    this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
  }
}
