import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'relativeDate',
  standalone: true
})
export class RelativeDatePipe implements PipeTransform {

  transform(value: string | Date): string {

    const date = new Date(value);
    const now = new Date();

    const diff = now.getTime() - date.getTime();

    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;

    const time = new Intl.DateTimeFormat('es-CO', {
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);

    if (diff < minute) {
      return `Hace unos segundos · ${time}`;
    }

    if (diff < hour) {
      const minutes = Math.floor(diff / minute);
      return `Hace ${minutes} minuto${minutes === 1 ? '' : 's'} · ${time}`;
    }

    if (diff < day) {
      const hours = Math.floor(diff / hour);
      return `Hace ${hours} hora${hours === 1 ? '' : 's'} · ${time}`;
    }

    if (diff < day * 2) {
      return `Ayer · ${time}`;
    }

    if (diff < day * 7) {
      const weekday = new Intl.DateTimeFormat('es-CO', {
        weekday: 'long'
      }).format(date);

      return `${weekday} · ${time}`;
    }

    const sameYear = date.getFullYear() === now.getFullYear();

    const formattedDate = new Intl.DateTimeFormat('es-CO', {
      day: 'numeric',
      month: 'short',
      ...(sameYear ? {} : { year: 'numeric' })
    }).format(date);

    return `${formattedDate} · ${time}`;
  }

}