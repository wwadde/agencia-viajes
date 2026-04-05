export interface ContenidoDTO {
    id: number;
    clave: string;
    valor: string;
}

export interface ContenidoUpsertDTO {
    clave: string;
    valor?: string;
}
