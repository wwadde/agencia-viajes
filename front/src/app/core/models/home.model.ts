import type { PaqueteResumenDTO } from './paquete.model';

export interface HomeLandingSlideDTO {
    referencia: string;
    url: string;
    thumbnailSmallUrl: string;
    thumbnailMediumUrl: string;
    nombre: string;
}

export interface HomeDataDTO {
    paquetes: PaqueteResumenDTO[];
    landingSlides: HomeLandingSlideDTO[];
}
