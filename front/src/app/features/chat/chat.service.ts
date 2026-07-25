import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatEvent, ChatRequest } from './chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  stream(request: ChatRequest): Observable<ChatEvent> {

    return new Observable<ChatEvent>(observer => {

      const controller = new AbortController();

      fetch(`${environment.apiUrl}/chatbot`, {
        method: 'POST',
        credentials: 'include',
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })
        .then(async response => {

          if (!response.ok) {
            observer.error(await response.text());
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

            if (done) {
              break;
            }

            buffer += decoder.decode(value, {
              stream: true
            });

            const rawEvents = buffer.split('\n\n');

            buffer = rawEvents.pop() ?? '';

            for (const rawEvent of rawEvents) {

              let eventName = 'message';
              let data = '';

              for (const line of rawEvent.split('\n')) {

                const trimmed = line.trim();

                if (!trimmed) {
                  continue;
                }

                if (trimmed.startsWith('event:')) {
                  eventName = trimmed.substring(6).trim();
                  continue;
                }

                if (trimmed.startsWith('data:')) {
                  data += trimmed.substring(5);
                }

              }

              observer.next({
                type: eventName,
                data
              });

            }

          }

          observer.complete();

        })
        .catch(error => {

          if (error.name !== 'AbortError') {
            observer.error(error);
          }

        });

      return () => controller.abort();

    });

  }

}