import { Pipe, PipeTransform, inject } from '@angular/core';
import { ImagenService } from '../services/imagen.service';

@Pipe({
    name: 'imagenMobileSrcset',
    standalone: true
})
export class ImagenMobileSrcsetPipe implements PipeTransform {
    private imagenService = inject(ImagenService);

    transform(referencia: string | undefined | null): string {
        if (!referencia) return '';
        return this.imagenService.buildMobileSrcset(referencia);
    }
}
