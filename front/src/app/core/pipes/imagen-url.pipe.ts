import { Pipe, PipeTransform, inject } from '@angular/core';
import { ImagenService } from '../services/imagen.service';

@Pipe({
    name: 'imagenUrl',
    standalone: true
})
export class ImagenUrlPipe implements PipeTransform {
    private imagenService = inject(ImagenService);

    transform(referencia: string | undefined | null): string {
        if (!referencia) return '';
        return this.imagenService.buildUrl(referencia);
    }
}
