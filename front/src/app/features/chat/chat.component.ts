import { Component, DestroyRef, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ChatMessage } from './chat.model';
import { ChatService } from './chat.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnDestroy {

  private readonly chat = inject(ChatService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authService = inject(AuthService);

  readonly authenticated = this.authService.isAuthenticated;

  private readonly pendingTokens: string[] = [];
  private typingInterval: number | null = null;
  private streamCompleted = false;

  open = signal(false);
  loading = signal(false);

  prompt = '';

  conversationId = Number(sessionStorage.getItem('conversationId') ?? '') || null;

  messages = signal<ChatMessage[]>([]);

  toggle(): void {
    this.open.update(value => !value);
  }

  send(): void {

    if (!this.prompt.trim() || this.loading()) {
      return;
    }

    this.pendingTokens.length = 0;
    this.streamCompleted = false;

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
      },
      {
        id: crypto.randomUUID(),
        role: 'CHATBOT',
        content: ''
      }
    ]);

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
              this.conversationId = Number(event.data);
              sessionStorage.setItem('conversationId', event.data);
              return;

            case 'message':
              this.pendingTokens.push(event.data);
              this.startTyping();
              return;

            case 'error':
              this.pendingTokens.length = 0;
              this.updateLastMessage(event.data);
              this.loading.set(false);
              return;

            default:
              return;
          }

        },

        error: error => {

          console.error(error);

          this.pendingTokens.length = 0;

          this.updateLastMessage(
            'Lo siento, ha ocurrido un error. Inténtalo nuevamente en unos momentos.'
          );

          this.loading.set(false);

        },

        complete: () => {

          this.streamCompleted = true;

        }

      });

  }

  private startTyping(): void {

    if (this.typingInterval !== null) {
      return;
    }

    this.typingInterval = window.setInterval(() => {

      const chunk = this.pendingTokens.splice(0, 2).join('');

      if (chunk) {

        this.messages.update(messages => {

          const copy = [...messages];

          const last = copy[copy.length - 1];

          copy[copy.length - 1] = {
            ...last,
            content: last.content + chunk
          };

          return copy;

        });

      }

      if (this.streamCompleted && this.pendingTokens.length === 0) {

        if (this.typingInterval !== null) {
          clearInterval(this.typingInterval);
          this.typingInterval = null;
        }

        this.loading.set(false);

      }

    }, 100); // modifica el intervalo

  }

  private updateLastMessage(content: string): void {

    this.messages.update(messages => {

      const copy = [...messages];

      copy[copy.length - 1] = {
        ...copy[copy.length - 1],
        content
      };

      return copy;

    });

  }

  ngOnDestroy(): void {

    if (this.typingInterval !== null) {
      clearInterval(this.typingInterval);
    }

  }

}