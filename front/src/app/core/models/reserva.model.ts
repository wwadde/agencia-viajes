export type ReservaEstado = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA';

export interface ReservaDTO {
    id: number;
    fechaReserva: string;
    estado: ReservaEstado;
    cantidadPersonas: number;
    precioUnitario: number;
    precioTotal: number;
    observaciones?: string;
    salidaId: number;
    fechaSalida: string;
    paqueteId: number;
    paqueteNombre: string;
    paqueteSlug: string;
    clienteId: number;
    clienteUsuario: string;
}

export interface ReservaCrearDTO {
    salidaId: number;
    cantidadPersonas: number;
    observaciones?: string;
}

export interface ReservaEstadoUpdateDTO {
    estado: ReservaEstado;
}
