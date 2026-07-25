import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ChatMessage } from './chat.model';
import { ChatService } from './chat.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent {

  private readonly chat = inject(ChatService);
  private readonly destroyRef = inject(DestroyRef);

  open = signal(false);
  loading = signal(false);

  prompt = '';

  conversationId: number | null = null;

  messages = signal<ChatMessage[]>([]);

  toggle(): void {
    this.open.update(value => !value);
  }

  send(): void {

    if (!this.prompt.trim() || this.loading()) {
      return;
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

        if (event.type !== 'message') {
          return;
        }

        this.messages.update(messages => {

          const copy = [...messages];

          const last = copy[copy.length - 1];

          copy[copy.length - 1] = {
            ...last,
            content: last.content + event.data
          };

          return copy;

        });

      },

      error: error => {

        console.error(error);

        this.loading.set(false);

      },

      complete: () => {

        this.loading.set(false);

      }

    });

  }

}