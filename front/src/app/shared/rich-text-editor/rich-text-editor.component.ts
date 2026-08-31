import { AfterViewInit, Component, ElementRef, forwardRef, inject, OnDestroy, PLATFORM_ID, ViewChild, ViewEncapsulation } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { Editor, JSONContent } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';

@Component({
  selector: 'app-rich-text-editor',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RichTextEditorComponent),
      multi: true
    }
  ],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="app-rich-text-editor space-y-3">
      <div class="editor-header">
        <div class="editor-header__title">Formato del itinerario</div>
        <div class="editor-header__hint">Usa títulos para secciones, listas para actividades y citas para destacar información importante.</div>
      </div>

      <div class="flex flex-wrap gap-2 border border-[var(--color-sea-strong)]/35 rounded-xl p-3 bg-[var(--color-cloud)] shadow-sm">
        <button type="button" class="editor-btn" [class.editor-btn-active]="isActive('bold')" (click)="toggleBold()" [disabled]="disabled" title="Negrita" aria-label="Negrita">B</button>
        <button type="button" class="editor-btn italic" [class.editor-btn-active]="isActive('italic')" (click)="toggleItalic()" [disabled]="disabled" title="Cursiva" aria-label="Cursiva">I</button>
        <button type="button" class="editor-btn" [class.editor-btn-active]="isHeadingActive()" (click)="toggleHeading()" [disabled]="disabled" title="Título de sección" aria-label="Título de sección">H2</button>
        <button type="button" class="editor-btn" [class.editor-btn-active]="isActive('bulletList')" (click)="toggleBulletList()" [disabled]="disabled" title="Lista con viñetas" aria-label="Lista con viñetas">• Lista</button>
        <button type="button" class="editor-btn" [class.editor-btn-active]="isActive('orderedList')" (click)="toggleOrderedList()" [disabled]="disabled" title="Lista numerada" aria-label="Lista numerada">1. Lista</button>
        <button type="button" class="editor-btn" [class.editor-btn-active]="isActive('blockquote')" (click)="toggleBlockquote()" [disabled]="disabled" title="Cita destacada" aria-label="Cita destacada">❝</button>
        <button type="button" class="editor-btn" (click)="undo()" [disabled]="disabled" title="Deshacer" aria-label="Deshacer">↶</button>
        <button type="button" class="editor-btn" (click)="redo()" [disabled]="disabled" title="Rehacer" aria-label="Rehacer">↷</button>
      </div>

      <div class="editor-shell min-h-56 rounded-xl border border-[var(--color-sea-strong)]/35 bg-white p-4 shadow-inner">
        <div #editorHost class="editor-content"></div>
      </div>
    </div>
  `,
  styles: [`
    .app-rich-text-editor {
      width: 100%;
    }

    .app-rich-text-editor .editor-header {
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
      margin-bottom: 0.35rem;
    }

    .app-rich-text-editor .editor-header__title {
      font-size: 0.78rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--color-muted);
    }

    .app-rich-text-editor .editor-header__hint {
      font-size: 0.75rem;
      color: var(--color-muted-soft);
      line-height: 1.4;
    }

    .app-rich-text-editor .editor-btn {
      border: 1px solid color-mix(in srgb, var(--color-sea-strong) 35%, transparent);
      border-radius: 0.7rem;
      background: white;
      color: var(--color-night);
      font-size: 0.75rem;
      font-weight: 700;
      padding: 0.45rem 0.68rem;
      min-height: 2.2rem;
      transition: 120ms ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.3rem;
    }

    .app-rich-text-editor .editor-btn.italic {
      font-style: italic;
      font-family: Georgia, serif;
    }

    .app-rich-text-editor .editor-btn:hover:enabled {
      border-color: color-mix(in srgb, var(--color-ink) 30%, transparent);
      background: color-mix(in srgb, var(--color-sea) 30%, white);
      transform: translateY(-1px);
    }

    .app-rich-text-editor .editor-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .app-rich-text-editor .editor-btn-active {
      border-color: var(--color-ink);
      background: color-mix(in srgb, var(--color-ink) 12%, white);
      box-shadow: inset 0 0 0 1px rgba(20, 27, 45, 0.08);
    }

    .app-rich-text-editor .editor-shell {
      background: #ffffff;
    }

    .app-rich-text-editor .editor-content .ProseMirror {
      min-height: 12rem;
      outline: none;
      color: var(--color-ink);
      line-height: 1.7;
      white-space: pre-wrap;
      padding: 0.1rem 0.2rem;
    }

    .app-rich-text-editor .editor-content .ProseMirror p {
      margin: 0.6rem 0;
    }

    .app-rich-text-editor .editor-content .ProseMirror p.is-editor-empty:first-child::before {
      color: var(--color-muted-soft);
      content: 'Escribe el detalle del paquete...';
      float: left;
      height: 0;
      pointer-events: none;
    }

    .app-rich-text-editor .editor-content .ProseMirror h2 {
      font-size: 1.15rem;
      font-weight: 800;
      margin-top: 1rem;
      margin-bottom: 0.55rem;
      color: var(--color-ink);
    }

    .app-rich-text-editor .editor-content .ProseMirror h3 {
      font-size: 1rem;
      font-weight: 700;
      margin-top: 0.85rem;
      margin-bottom: 0.4rem;
      color: var(--color-ink);
    }

    .app-rich-text-editor .editor-content .ProseMirror ul {
      list-style: disc;
      margin-left: 1.25rem;
      margin-top: 0.4rem;
      margin-bottom: 0.4rem;
    }

    .app-rich-text-editor .editor-content .ProseMirror ol {
      list-style: decimal;
      margin-left: 1.25rem;
      margin-top: 0.4rem;
      margin-bottom: 0.4rem;
    }

    .app-rich-text-editor .editor-content .ProseMirror blockquote {
      border-left: 3px solid color-mix(in srgb, var(--color-ink) 28%, transparent);
      margin: 0.75rem 0;
      padding-left: 0.75rem;
      color: var(--color-muted);
      background: rgba(175, 206, 255, 0.08);
    }

    .app-rich-text-editor .editor-content .ProseMirror strong {
      font-weight: 700;
    }

    .app-rich-text-editor .editor-content .ProseMirror em {
      font-style: italic;
    }
  `]
})
export class RichTextEditorComponent implements ControlValueAccessor, AfterViewInit, OnDestroy {
  @ViewChild('editorHost', { static: true }) editorHost!: ElementRef<HTMLDivElement>;

  private readonly platformId = inject(PLATFORM_ID);
  editor: Editor | null = null;
  private value: JSONContent = RichTextEditorComponent.emptyDoc();

  disabled = false;
  private onChange: (value: JSONContent) => void = () => {};
  private onTouched: () => void = () => {};

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.editor = new Editor({
      element: this.editorHost.nativeElement,
      extensions: [
        StarterKit.configure({
          heading: {
            levels: [2, 3]
          }
        }),
        Placeholder.configure({
          placeholder: 'Escribe el detalle del paquete...'
        })
      ],
      content: this.value,
      editable: !this.disabled,
      onUpdate: ({ editor }) => {
        this.value = editor.getJSON();
        this.onChange(this.value);
      },
      onBlur: () => this.onTouched()
    });
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
  }

  writeValue(value: unknown): void {
    this.value = this.normalizeValue(value);
    this.editor?.commands.setContent(this.value, { emitUpdate: false });
  }

  registerOnChange(fn: (value: JSONContent) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.editor?.setEditable(!isDisabled);
  }

  isActive(name: string): boolean {
    return this.editor?.isActive(name) ?? false;
  }

  isHeadingActive(): boolean {
    return this.editor?.isActive('heading', { level: 2 }) ?? false;
  }

  isHeadingLevelActive(level: 2 | 3): boolean {
    return this.editor?.isActive('heading', { level }) ?? false;
  }

  toggleBold(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleBold().run());
  }

  toggleItalic(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleItalic().run());
  }

  toggleHeading(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleHeading({ level: 2 }).run());
  }

  toggleHeadingLevel(level: 2 | 3): void {
    this.runCommand(() => this.editor?.chain().focus().toggleHeading({ level }).run());
  }

  toggleBulletList(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleBulletList().run());
  }

  toggleOrderedList(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleOrderedList().run());
  }

  toggleBlockquote(): void {
    this.runCommand(() => this.editor?.chain().focus().toggleBlockquote().run());
  }

  undo(): void {
    this.runCommand(() => this.editor?.chain().focus().undo().run());
  }

  redo(): void {
    this.runCommand(() => this.editor?.chain().focus().redo().run());
  }

  private runCommand(command: () => void): void {
    if (this.disabled || !this.editor) {
      return;
    }
    command();
    this.onTouched();
  }

  private normalizeValue(value: unknown): JSONContent {
    if (this.isJsonContent(value)) {
      return value;
    }

    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) {
        return RichTextEditorComponent.emptyDoc();
      }

      const parsed = this.tryParseJsonContent(trimmed);
      if (parsed) {
        return parsed;
      }

      return {
        type: 'doc',
        content: [
          {
            type: 'paragraph',
            content: [{ type: 'text', text: trimmed }]
          }
        ]
      };
    }

    return RichTextEditorComponent.emptyDoc();
  }

  private tryParseJsonContent(value: string): JSONContent | null {
    try {
      const parsed: unknown = JSON.parse(value);
      return this.isJsonContent(parsed) ? parsed : null;
    } catch {
      return null;
    }
  }

  private isJsonContent(value: unknown): value is JSONContent {
    if (typeof value !== 'object' || value === null) {
      return false;
    }
    const withType = value as { type?: unknown };
    return typeof withType.type === 'string';
  }

  private static emptyDoc(): JSONContent {
    return {
      type: 'doc',
      content: [{ type: 'paragraph' }]
    };
  }
}
