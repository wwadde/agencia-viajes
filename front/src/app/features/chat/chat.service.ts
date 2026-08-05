import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatEvent, ChatRequest, ConversacionResponse, ConversacionResumen, PageResponse } from './chat.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private readonly http = inject(HttpClient);

    getConversation(id: string): Observable<ConversacionResponse> {
    return this.http.get<ConversacionResponse>(
      `${environment.apiUrl}/chatbot/conversaciones/${id}`
    );
  }

  getConversations(page = 0, size = 10) {
  return this.http.get<PageResponse<ConversacionResumen>>(
    `${environment.apiUrl}/chatbot/conversaciones`,
    {
      params: {
        page,
        size
      }
    }
  );
}

  stream(request: ChatRequest): Observable<ChatEvent> {
    return new Observable<ChatEvent>(observer => {
      const controller = new AbortController();

      // Lee el token CSRF de la cookie (mismo nombre que usa Angular)
      const csrfToken = this.getCookie('XSRF-TOKEN');

      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };

      // Añade el header solo si existe el token
      if (csrfToken) {
        headers['X-XSRF-TOKEN'] = csrfToken;
      }

      fetch(`${environment.apiUrl}/chatbot`, {
        method: 'POST',
        credentials: 'include',
        signal: controller.signal,
        headers,
        body: JSON.stringify(request)
      })
        .then(async response => {
          if (!response.ok) {
            // Intentamos parsear el error JSON de Spring (ProblemDetail)
            let errorBody: any;
            try {
              errorBody = await response.json();
            } catch {
              errorBody = await response.text();
            }
            observer.error(errorBody);
            return;
          }

          if (!response.body) {
            observer.complete();
            return;
          }

          const reader = response.body.getReader();
          const decoder = new TextDecoder();
          let buffer = '';

          while (true) {
            const { value, done } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });

            const rawEvents = buffer.split('\n\n');
            buffer = rawEvents.pop() ?? '';

            for (const rawEvent of rawEvents) {
              let eventName = 'message';
              let data = '';

              for (const line of rawEvent.split('\n')) {
                const trimmed = line.trim();
                if (!trimmed) continue;

                if (trimmed.startsWith('event:')) {
                  eventName = trimmed.substring(6).trim();
                } else if (trimmed.startsWith('data:')) {
                  // Concatenamos por si hay varias líneas data:
                  data += (data ? '\n' : '') + trimmed.substring(5);
                }
              }

              if (data) {
                observer.next({ type: eventName, data });
              }
            }
          }

          observer.complete();
        })
        .catch(error => {
          if (error.name !== 'AbortError') {
            observer.error(error);
          }
        });

      // Cleanup al unsubscribe
      return () => controller.abort();
    });
  }

  /** Lee una cookie por nombre */
  private getCookie(name: string): string | null {
    const matches = document.cookie.match(
      new RegExp('(?:^|; )' + name.replace(/([.$?*|{}()[\]\\/+^])/g, '\\$1') + '=([^;]*)')
    );
    return matches ? decodeURIComponent(matches[1]) : null;
  }

  
}

