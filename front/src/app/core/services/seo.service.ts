import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { PaqueteDetalleDTO } from '../models/paquete.model';
import { ImagenService } from './imagen.service';

@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly siteOrigin = 'https://agencia.williamwadde.work';

  private readonly meta = inject(Meta);
  private readonly title = inject(Title);
  private readonly document = inject(DOCUMENT);
  private readonly imagenService = inject(ImagenService);

  setHomeDefaults(): void {
    const homeUrl = `${this.siteOrigin}/`;
    const defaultTitle = 'Agencia de Viajes | Paquetes, Reservas y Experiencias';
    const defaultDescription =
      'Descubre paquetes turisticos, reserva tus proximos viajes y vive experiencias unicas con nuestra agencia de viajes.';

    this.title.setTitle(defaultTitle);
    this.meta.updateTag({ name: 'description', content: defaultDescription });
    this.meta.updateTag({
      name: 'keywords',
      content:
        'agencia de viajes, paquetes turisticos, reservas, turismo, vacaciones, viajes, Ibague, Tolima, Colombia',
    });

    this.meta.updateTag({ property: 'og:type', content: 'website' });
    this.meta.updateTag({ property: 'og:locale', content: 'es_ES' });
    this.meta.updateTag({ property: 'og:site_name', content: 'Agencia de Viajes' });
    this.meta.updateTag({ property: 'og:title', content: defaultTitle });
    this.meta.updateTag({
      property: 'og:description',
      content: 'Explora destinos, compara paquetes y reserva tu viaje ideal en minutos.',
    });
    this.meta.updateTag({ property: 'og:url', content: homeUrl });
    this.meta.updateTag({ property: 'og:image', content: `${this.siteOrigin}/favicon.ico` });

    this.meta.updateTag({ name: 'twitter:card', content: 'summary_large_image' });
    this.meta.updateTag({ name: 'twitter:title', content: defaultTitle });
    this.meta.updateTag({
      name: 'twitter:description',
      content: 'Explora destinos, compara paquetes y reserva tu viaje ideal en minutos.',
    });
    this.meta.updateTag({ name: 'twitter:image', content: `${this.siteOrigin}/favicon.ico` });

    this.updateCanonical(homeUrl);
  }

  setPackageSeo(paquete: PaqueteDetalleDTO, slug: string): void {
    const pageTitle = `${paquete.nombre} | ${paquete.destino.nombre} | Agencia de Viajes`;
    const description = this.truncate(
      paquete.descripcion ||
        `Descubre ${paquete.nombre} en ${paquete.destino.nombre}. Reserva tu viaje ahora.`,
      160,
    );
    const paqueteUrl = `${this.siteOrigin}/paquetes/${slug}`;
    const imageReference =
      paquete.imagenes?.find((img) => img.principal)?.url ?? paquete.imagenes?.[0]?.url;
    const imageUrl = imageReference
      ? this.toAbsoluteUrl(this.imagenService.buildUrl(imageReference))
      : `${this.siteOrigin}/favicon.ico`;

    this.title.setTitle(this.truncate(pageTitle, 60));
    this.meta.updateTag({ name: 'description', content: description });
    this.meta.updateTag({
      name: 'keywords',
      content: `${paquete.nombre}, ${paquete.destino.nombre}, paquetes turisticos, viajes`,
    });

    this.meta.updateTag({ property: 'og:title', content: pageTitle });
    this.meta.updateTag({ property: 'og:description', content: description });
    this.meta.updateTag({ property: 'og:url', content: paqueteUrl });
    this.meta.updateTag({ property: 'og:image', content: imageUrl });

    this.meta.updateTag({ name: 'twitter:title', content: pageTitle });
    this.meta.updateTag({ name: 'twitter:description', content: description });
    this.meta.updateTag({ name: 'twitter:image', content: imageUrl });

    this.updateCanonical(paqueteUrl);
  }

  setPackageNotFoundSeo(): void {
    this.title.setTitle('Paquete no encontrado | Agencia de Viajes');
    this.meta.updateTag({
      name: 'description',
      content: 'El paquete solicitado no esta disponible o no existe.',
    });
  }

  private updateCanonical(url: string): void {
    let canonical = this.document.querySelector('link[rel="canonical"]') as HTMLLinkElement | null;

    if (!canonical) {
      canonical = this.document.createElement('link');
      canonical.setAttribute('rel', 'canonical');
      this.document.head.appendChild(canonical);
    }

    canonical.setAttribute('href', url);
  }

  private toAbsoluteUrl(url: string): string {
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }

    if (url.startsWith('/')) {
      return `${this.siteOrigin}${url}`;
    }

    return `${this.siteOrigin}/${url}`;
  }

  private truncate(value: string, max: number): string {
    if (value.length <= max) {
      return value;
    }

    return `${value.slice(0, max - 3).trim()}...`;
  }
}
