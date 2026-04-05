import type { DestinoDTO } from './destino.model';

// ── Public read DTOs ────────────────────────────────────────────────

export interface PaqueteResumenDTO {
    id: number;
    nombre: string;
    slug: string;
    descripcion: string;
    duracionDias: number;
    destacado: boolean;
    destino: string;
    imagenPrincipal: string;
}

export interface PaqueteDetalleDTO {
    id: number;
    nombre: string;
    slug: string;
    descripcion: string;
    detalle: string;
    duracionDias: number;
    disponible: boolean;
    destacado: boolean;
    destino: DestinoDTO;
    imagenes: ImagenPaqueteDTO[];
    salidas: SalidaDTO[];
}

export interface ImagenPaqueteDTO {
    id: number;
    url: string;
    orden: number;
    principal: boolean;
}

export interface SalidaDTO {
    id: number;
    fechaSalida: string;
    precio: number;
    cuposDisponibles: number;
}

// ── Admin write DTOs ────────────────────────────────────────────────

export interface PaqueteUpsertDTO {
    nombre: string;
    slug?: string;
    descripcion: string;
    detalle: string;
    duracionDias: number;
    disponible: boolean;
    destacado: boolean;
    destinoId: number;
    imagenes?: ImagenPaqueteUpsertDTO[];
    salidas?: SalidaUpsertDTO[];
}

export interface ImagenPaqueteUpsertDTO {
    referencia: string;
    orden: number;
    principal: boolean;
}

export interface SalidaUpsertDTO {
    fechaSalida: string;
    precio: number;
    cuposDisponibles: number;
}
