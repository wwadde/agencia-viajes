export interface AgendaSalidaDTO {
    id: number;
    fechaSalida: string;
    precio: number;
    cuposDisponibles: number;
    paqueteId: number;
    paqueteNombre: string;
    paqueteSlug: string;
}

export interface AgendaSalidaUpsertDTO {
    fechaSalida: string;
    precio: number;
    cuposDisponibles: number;
    paqueteId: number;
}
