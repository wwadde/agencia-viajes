import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminForoService } from '../../../core/services/admin-foro.service';
import { ForoBloqueTipo, ForoComentarioDTO, ForoPostUpsertDTO } from '../../../core/models/foro.model';
import { ImagenSelectorComponent } from '../imagenes/imagen-selector.component';
import { ImagenUrlPipe } from '../../../core/pipes/imagen-url.pipe';

@Component({
  selector: 'app-admin-foro-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, ImagenSelectorComponent, ImagenUrlPipe, DatePipe],
  template: `
    <div class="max-w-5xl mx-auto space-y-8 pb-12">
      <div class="flex items-center gap-4">
        <a routerLink="/admin/foro" class="p-2 rounded-xl border border-[var(--color-sea-strong)]/35 hover:bg-white/70 transition-colors">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-[var(--color-night)] tracking-tight">{{ isEditMode() ? 'Editar publicación' : 'Nueva publicación del foro' }}</h1>
          <p class="text-sm text-[var(--color-muted)] mt-1">Estructura visual por bloques de texto e imagen.</p>
        </div>
      </div>

      @if (saveError() || foroService.error()) {
        <div class="rounded-xl border border-red-500/35 bg-red-900/15 p-4 text-sm text-red-700">{{ saveError() || foroService.error() }}</div>
      }

      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-8">
        <section class="rounded-3xl border border-[var(--color-sea-strong)]/35 bg-white/90 p-6 sm:p-8 space-y-6">
          <h2 class="text-lg font-bold text-[var(--color-night)]">Datos principales</h2>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Título *</label>
              <input type="text" formControlName="titulo" (input)="generateSlug()" class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/20" />
            </div>
            <div>
              <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Slug</label>
              <input type="text" formControlName="slug" class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-3 bg-[var(--color-sea)]/35 font-mono text-sm focus:outline-none" />
            </div>
            <div class="flex items-end">
              <label class="inline-flex items-center gap-2 text-sm font-medium text-[var(--color-night)] cursor-pointer">
                <input type="checkbox" formControlName="publicado" class="w-4 h-4" />
                Publicado
              </label>
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-[var(--color-muted)] mb-1.5">Resumen *</label>
              <textarea rows="4" formControlName="resumen" class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/20" placeholder="Describe de forma atractiva de qué trata el artículo..."></textarea>
            </div>
          </div>
        </section>

        <section formArrayName="bloques" class="rounded-3xl border border-[var(--color-sea-strong)]/35 bg-white/90 p-6 sm:p-8 space-y-5">
          <div class="flex items-center justify-between gap-3">
            <h2 class="text-lg font-bold text-[var(--color-night)]">Bloques del post</h2>
            <div class="flex items-center gap-2">
              <button type="button" (click)="addTextBlock()" class="px-3 py-1.5 rounded-lg border border-[var(--color-sea-strong)]/45 bg-[var(--color-sea)]/35 text-sm font-medium">+ Texto</button>
              <button type="button" (click)="addImageBlock()" class="px-3 py-1.5 rounded-lg border border-[var(--color-sea-strong)]/45 bg-[var(--color-sea)]/35 text-sm font-medium">+ Imagen</button>
            </div>
          </div>

          @if (bloques.length === 0) {
            <p class="text-sm text-[var(--color-muted)]">Agrega al menos un bloque para construir el artículo.</p>
          }

          @for (bloque of bloques.controls; track $index) {
            <article [formGroupName]="$index" class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white p-4 space-y-4">
              <div class="flex flex-wrap items-center gap-3">
                <span class="text-xs uppercase tracking-[0.15em] text-[var(--color-muted-soft)]">Bloque {{ $index + 1 }}</span>
                <select formControlName="tipo" class="rounded-lg border border-[var(--color-sea-strong)]/35 px-3 py-1.5 text-sm bg-white" (change)="onTypeChange($index)">
                  <option [value]="'TEXTO'">Texto</option>
                  <option [value]="'IMAGEN'">Imagen</option>
                </select>
                <div class="ml-auto flex items-center gap-2">
                  <button type="button" (click)="moveBlockUp($index)" [disabled]="$index === 0" class="px-2 py-1 rounded border border-[var(--color-sea-strong)]/45 text-sm disabled:opacity-50">↑</button>
                  <button type="button" (click)="moveBlockDown($index)" [disabled]="$index === bloques.length - 1" class="px-2 py-1 rounded border border-[var(--color-sea-strong)]/45 text-sm disabled:opacity-50">↓</button>
                  <button type="button" (click)="removeBlock($index)" class="px-2 py-1 rounded border border-red-500/35 text-sm text-red-600">Quitar</button>
                </div>
              </div>

              @if (bloque.get('tipo')?.value === 'TEXTO') {
                <textarea rows="5" formControlName="contenido" class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-3 bg-white focus:outline-none focus:ring-2 focus:ring-[var(--color-ink)]/20" placeholder="Contenido textual del bloque..."></textarea>
              } @else {
                <div class="grid grid-cols-1 md:grid-cols-[140px_1fr] gap-4 items-start">
                  <button type="button" (click)="openImageSelector($index)" class="w-[140px] h-[110px] rounded-xl border border-[var(--color-sea-strong)]/35 bg-[var(--color-cloud)] overflow-hidden flex items-center justify-center">
                    @if (bloque.get('contenido')?.value) {
                      <img [src]="bloque.get('contenido')?.value | imagenUrl" class="w-full h-full object-cover" />
                    } @else {
                      <span class="text-xs text-[var(--color-muted)]">Elegir imagen</span>
                    }
                  </button>
                  <div class="space-y-3">
                    <input type="text" formControlName="contenido" readonly class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-2.5 bg-[var(--color-sea)]/35 text-sm" placeholder="Referencia de imagen (foro/archivo.webp)" />
                    <input type="text" formControlName="pieImagen" class="w-full rounded-xl border border-[var(--color-sea-strong)]/35 px-4 py-2.5 bg-white text-sm" placeholder="Pie de imagen (opcional)" />
                  </div>
                </div>
              }
            </article>
          }
        </section>

        @if (isEditMode()) {
          <section class="rounded-3xl border border-[var(--color-sea-strong)]/35 bg-white/90 p-6 sm:p-8 space-y-5">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-lg font-bold text-[var(--color-night)]">Moderación de comentarios</h2>
              <span class="text-sm text-[var(--color-muted)]">{{ comentarios().length }} total</span>
            </div>

            @if (comentarios().length === 0) {
              <p class="text-sm text-[var(--color-muted)]">Esta publicación aún no tiene comentarios.</p>
            } @else {
              <div class="space-y-3">
                @for (comentario of comentarios(); track comentario.id) {
                  <article class="rounded-2xl border border-[var(--color-sea-strong)]/35 bg-white p-4">
                    <div class="flex flex-wrap items-center justify-between gap-3 mb-2">
                      <div class="flex items-center gap-2">
                        <strong class="text-sm text-[var(--color-night)]">{{ comentario.autor }}</strong>
                        @if (comentario.oculto) {
                          <span class="text-[10px] uppercase tracking-[0.12em] px-2 py-1 rounded-full bg-amber-100 text-amber-800">Oculto</span>
                        }
                      </div>
                      <span class="text-xs text-[var(--color-muted-soft)]">{{ comentario.fechaCreacion | date:'dd/MM/yyyy HH:mm' }}</span>
                    </div>

                    <p class="text-sm text-[var(--color-muted)] whitespace-pre-wrap leading-relaxed">{{ comentario.contenido }}</p>

                    <div class="mt-3 flex flex-wrap items-center gap-2">
                      <button
                        type="button"
                        (click)="toggleComentarioOculto(comentario)"
                        [disabled]="moderatingCommentId() === comentario.id"
                        class="px-3 py-1.5 rounded-lg border border-[var(--color-sea-strong)]/45 text-sm text-[var(--color-night)] hover:bg-[var(--color-sea)]/35 disabled:opacity-55"
                      >
                        {{ comentario.oculto ? 'Mostrar' : 'Ocultar' }}
                      </button>
                      <button
                        type="button"
                        (click)="removeComentario(comentario.id)"
                        [disabled]="moderatingCommentId() === comentario.id"
                        class="px-3 py-1.5 rounded-lg border border-red-500/35 text-sm text-red-600 hover:bg-red-500/10 disabled:opacity-55"
                      >
                        Eliminar
                      </button>
                      <span class="ml-auto text-xs text-[var(--color-muted-soft)]">{{ comentario.totalLikes }} me gusta</span>
                    </div>
                  </article>
                }
              </div>
            }
          </section>
        }

        <div class="flex items-center justify-end gap-3">
          <a routerLink="/admin/foro" class="px-4 py-2.5 rounded-xl border border-[var(--color-sea-strong)]/45 text-[var(--color-muted)] hover:bg-white/70 transition-colors">Cancelar</a>
          <button type="submit" [disabled]="form.invalid || isSaving()" class="px-5 py-2.5 rounded-xl bg-[var(--color-ink)] text-white font-semibold hover:bg-[var(--color-night)] disabled:bg-[var(--color-sea-strong)] disabled:text-[var(--color-muted)] transition-colors">
            {{ isSaving() ? 'Guardando...' : (isEditMode() ? 'Actualizar post' : 'Crear post') }}
          </button>
        </div>
      </form>
    </div>

    <app-imagen-selector
      [isOpen]="selectorOpen()"
      [initialFolder]="'foro'"
      (close)="selectorOpen.set(false)"
      (selectImage)="onImageSelected($event)"
    ></app-imagen-selector>
  `,
})
export default class ForoFormComponent implements OnInit {
  readonly foroService = inject(AdminForoService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly isEditMode = signal(false);
  readonly postId = signal<number | null>(null);
  readonly isSaving = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly comentarios = signal<ForoComentarioDTO[]>([]);
  readonly moderatingCommentId = signal<number | null>(null);

  readonly selectorOpen = signal(false);
  readonly selectedBlockIndex = signal<number | null>(null);

  readonly form = this.fb.group({
    titulo: ['', [Validators.required, Validators.maxLength(180)]],
    slug: [''],
    resumen: ['', [Validators.required, Validators.maxLength(2000)]],
    publicado: [true, Validators.required],
    bloques: this.fb.array([]),
  });

  get bloques() {
    return this.form.get('bloques') as FormArray<FormGroup>;
  }

  async ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam && idParam !== 'nuevo') {
      const id = Number(idParam);
      this.isEditMode.set(true);
      this.postId.set(id);
      await this.loadPost(id);
      return;
    }

    this.addTextBlock();
  }

  private createBlock(tipo: ForoBloqueTipo, contenido = '', pieImagen: string | null = null, orden = 0) {
    return this.fb.group({
      tipo: [tipo, Validators.required],
      contenido: [contenido, [Validators.required, Validators.maxLength(5000)]],
      pieImagen: [pieImagen],
      orden: [orden, [Validators.required, Validators.min(0)]],
    });
  }

  addTextBlock() {
    this.bloques.push(this.createBlock('TEXTO', '', null, this.bloques.length));
    this.syncOrders();
  }

  addImageBlock() {
    this.bloques.push(this.createBlock('IMAGEN', '', '', this.bloques.length));
    this.syncOrders();
  }

  removeBlock(index: number) {
    this.bloques.removeAt(index);
    this.syncOrders();
  }

  moveBlockUp(index: number) {
    if (index <= 0) {
      return;
    }

    const current = this.bloques.at(index);
    const previous = this.bloques.at(index - 1);
    this.bloques.setControl(index - 1, current);
    this.bloques.setControl(index, previous);
    this.syncOrders();
  }

  moveBlockDown(index: number) {
    if (index >= this.bloques.length - 1) {
      return;
    }

    const current = this.bloques.at(index);
    const next = this.bloques.at(index + 1);
    this.bloques.setControl(index + 1, current);
    this.bloques.setControl(index, next);
    this.syncOrders();
  }

  onTypeChange(index: number) {
    const group = this.bloques.at(index);
    const type = group.get('tipo')?.value as ForoBloqueTipo;
    if (type === 'TEXTO') {
      group.patchValue({ pieImagen: null });
    }
  }

  openImageSelector(index: number) {
    this.selectedBlockIndex.set(index);
    this.selectorOpen.set(true);
  }

  onImageSelected(ref: string) {
    const index = this.selectedBlockIndex();
    if (index === null) {
      return;
    }

    this.bloques.at(index).patchValue({ contenido: ref });
    this.selectorOpen.set(false);
  }

  generateSlug() {
    if (this.isEditMode()) {
      return;
    }

    const title = this.form.controls.titulo.value || '';
    const slug = title
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9\s-]/g, '')
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-+|-+$/g, '');

    this.form.controls.slug.setValue(slug, { emitEvent: false });
  }

  private syncOrders() {
    for (let i = 0; i < this.bloques.length; i++) {
      this.bloques.at(i).get('orden')?.setValue(i, { emitEvent: false });
    }
  }

  private async loadPost(id: number) {
    const post = await this.foroService.getPost(id);
    if (!post) {
      return;
    }

    this.form.patchValue({
      titulo: post.titulo,
      slug: post.slug,
      resumen: post.resumen,
      publicado: post.publicado,
    });

    this.bloques.clear();
    post.bloques
      .slice()
      .sort((a, b) => a.orden - b.orden)
      .forEach((block) => {
        this.bloques.push(this.createBlock(block.tipo, block.contenido, block.pieImagen, block.orden));
      });

    this.comentarios.set(
      post.comentarios
        .slice()
        .sort((a, b) => new Date(b.fechaCreacion).getTime() - new Date(a.fechaCreacion).getTime())
    );

    this.syncOrders();
  }

  async toggleComentarioOculto(comentario: ForoComentarioDTO) {
    this.moderatingCommentId.set(comentario.id);
    this.saveError.set(null);
    try {
      await this.foroService.setComentarioOculto(comentario.id, !comentario.oculto);
      this.comentarios.update((items) => items.map((item) => item.id === comentario.id
        ? { ...item, oculto: !item.oculto }
        : item));
    } catch {
      this.saveError.set('No se pudo actualizar la visibilidad del comentario.');
    } finally {
      this.moderatingCommentId.set(null);
    }
  }

  async removeComentario(comentarioId: number) {
    if (!confirm('¿Eliminar este comentario de forma permanente?')) {
      return;
    }

    this.moderatingCommentId.set(comentarioId);
    this.saveError.set(null);
    try {
      await this.foroService.deleteComentario(comentarioId);
      this.comentarios.update((items) => items.filter((item) => item.id !== comentarioId));
    } catch {
      this.saveError.set('No se pudo eliminar el comentario.');
    } finally {
      this.moderatingCommentId.set(null);
    }
  }

  async submit() {
    if (this.form.invalid || this.bloques.length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    this.syncOrders();
    this.isSaving.set(true);
    this.saveError.set(null);

    const data: ForoPostUpsertDTO = {
      titulo: this.form.controls.titulo.value ?? '',
      slug: this.form.controls.slug.value ?? '',
      resumen: this.form.controls.resumen.value ?? '',
      publicado: !!this.form.controls.publicado.value,
      bloques: this.bloques.controls.map((group, index) => ({
        tipo: group.get('tipo')?.value as ForoBloqueTipo,
        contenido: String(group.get('contenido')?.value ?? '').trim(),
        pieImagen: String(group.get('pieImagen')?.value ?? '').trim() || null,
        orden: index,
      })),
    };

    try {
      if (this.isEditMode() && this.postId()) {
        await this.foroService.updatePost(this.postId()!, data);
      } else {
        await this.foroService.createPost(data);
      }

      this.router.navigate(['/admin/foro']);
    } catch {
      this.saveError.set('No se pudo guardar la publicación. Revisa los campos e intenta de nuevo.');
    } finally {
      this.isSaving.set(false);
    }
  }
}
