import { Component, DestroyRef, OnDestroy, OnInit, inject, signal, ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ChatMessage, ChatMessageRole, ConversacionResumen } from './chat.model';
import { ChatService } from './chat.service';
import { AuthService } from '../../core/services/auth.service';
import { RelativeDatePipe } from '../../core/pipes/relative-dates.pipe';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, RelativeDatePipe],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnDestroy, OnInit {

  private readonly chat = inject(ChatService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);

  readonly authenticated = this.authService.isAuthenticated;

  @ViewChild('messagesContainer')
  private messagesContainer?: ElementRef<HTMLDivElement>;

  private readonly pendingTokens: string[] = [];
  private typingInterval: number | null = null;
  private streamCompleted = false;
  private assistantMessageId: string | null = null;

  open = signal(false);
  closing = signal(false);
  loading = signal(false);
  showHistory = signal(false);

  prompt = '';

  conversationId: string | null = sessionStorage.getItem('conversationId');

  messages = signal<ChatMessage[]>([]);
  conversations = signal<ConversacionResumen[]>([]);

  page = 0;
  lastPage = false;

  toggle(): void {

    if (!this.open()) {

      this.open.set(true);

      requestAnimationFrame(() => this.scrollToBottom());

      return;

    }

    this.closing.set(true);

    setTimeout(() => {

      this.open.set(false);

      this.closing.set(false);

    }, 220);

  }

  toggleHistory(): void {

    if (!this.showHistory() && this.conversations().length === 0) {
      this.loadConversations();
    }

    this.showHistory.update(value => !value);

  }

  selectConversation(id: string): void {

    this.chat.getConversation(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: conversacion => {

          this.conversationId = conversacion.id;

          sessionStorage.setItem(
            'conversationId',
            conversacion.id
          );

          this.messages.set(

            conversacion.mensajes.map(mensaje => ({

              id: mensaje.id,

              role: mensaje.rolMensaje,

              content: mensaje.mensaje

            }))

          );

          this.scrollToBottom();

          this.showHistory.set(false);

        },

        error: console.error

      });

  }

  loadConversations(): void {

    if (this.lastPage) {
      return;
    }

    this.chat.getConversations(this.page)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: response => {

          this.conversations.update(list => [
            ...list,
            ...response.content
          ]);

          this.page++;

          this.lastPage = response.last;

        },

        error: console.error

      });

  }

  newConversation(): void {

    this.conversationId = null;

    sessionStorage.removeItem('conversationId');

    this.messages.set([]);

    this.prompt = '';

    this.pendingTokens.length = 0;

    this.loading.set(false);

    this.showHistory.set(false);

    requestAnimationFrame(() => this.scrollToBottom());

  }


  private addStatusMessage(role: ChatMessageRole, content: string): void {

    this.messages.update(messages => [
      ...messages,
      {
        id: crypto.randomUUID(),
        role,
        content
      }
    ]);

    this.scrollToBottom();
  }

  private ensureAssistantMessage(): void {

    if (this.assistantMessageId !== null) {
      return;
    }

    this.assistantMessageId = crypto.randomUUID();

    this.messages.update(messages => [
      ...messages,
      {
        id: this.assistantMessageId!,
        role: 'CHATBOT',
        content: ''
      }
    ]);

    this.scrollToBottom();
  }

  send(): void {

    if (!this.prompt.trim() || this.loading()) {
      return;
    }

    this.pendingTokens.length = 0;
    this.streamCompleted = false;
    this.assistantMessageId = null;

    if (this.typingInterval !== null) {
      clearInterval(this.typingInterval);
      this.typingInterval = null;
    }

    const question = this.prompt.trim();

    this.messages.update(messages => [
      ...messages,
      {
        id: crypto.randomUUID(),
        role: 'USUARIO',
        content: question
      }
    ]);

    this.scrollToBottom();

    this.prompt = '';

    this.loading.set(true);

    this.chat.stream({
      peticion: question,
      conversacionId: this.conversationId
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: event => {

          switch (event.type) {

            case 'conversationId':
              this.conversationId = event.data;
              sessionStorage.setItem('conversationId', event.data);
              this.conversations.set([]);
              this.page = 0;
              this.lastPage = false;
              return;

            case 'message':
              this.ensureAssistantMessage();
              this.pendingTokens.push(event.data);
              this.startTyping();
              return;

            case 'tool':
              this.pendingTokens.length = 0;
              this.addStatusMessage('TOOL', event.data);
              return;

            case 'error':
              this.pendingTokens.length = 0;
              this.addStatusMessage('ERROR', event.data);
              this.loading.set(false);
              return;

            default:
              return;
          }

        },

        error: error => {

          this.pendingTokens.length = 0;

          const message =
            error?.detail ??
            error?.message ??
            'Lo siento, ha ocurrido un error. Inténtalo nuevamente en unos momentos.';

          this.addStatusMessage('ERROR', message);

          this.loading.set(false);

        },

        complete: () => {

          this.streamCompleted = true;

          if (
            this.pendingTokens.length === 0 &&
            this.typingInterval === null
          ) {
            this.loading.set(false);
          }

        }

      });

  }

  private startTyping(): void {

    if (this.typingInterval !== null) {
      return;
    }

    this.typingInterval = window.setInterval(() => {

      const chunk = this.pendingTokens.shift();

      if (chunk && this.assistantMessageId !== null) {

        this.messages.update(messages =>
          messages.map(message =>
            message.id === this.assistantMessageId
              ? {
                ...message,
                content: message.content + chunk
              }
              : message
          )
        );

        this.scrollToBottom();
      }

      if (
        this.streamCompleted &&
        this.pendingTokens.length === 0
      ) {

        if (this.typingInterval !== null) {
          clearInterval(this.typingInterval);
          this.typingInterval = null;
        }

        this.loading.set(false);
      }

    }, 50);
  }


  private scrollToBottom(): void {

    requestAnimationFrame(() => {

      const container = this.messagesContainer?.nativeElement;

      if (!container) {
        return;
      }

      container.scrollTop = container.scrollHeight;

    });

  }

  ngOnDestroy(): void {

    if (this.typingInterval !== null) {
      clearInterval(this.typingInterval);
    }

  }

  ngOnInit(): void {

    if (!this.conversationId) {
      return;
    }

    this.chat.getConversation(this.conversationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({

        next: conversacion => {

          this.messages.set(
            conversacion.mensajes.map(mensaje => ({
              id: mensaje.id,
              role: mensaje.rolMensaje,
              content: mensaje.mensaje
            }))
          );

          this.scrollToBottom();

        },

        error: error => {

          console.error(error);

          sessionStorage.removeItem('conversationId');
          this.conversationId = null;

        }

      });

  }

}