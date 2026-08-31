export interface DestinoDTO {
    id: number;
    nombre: string;
    pais: string;
    ciudad: string;
    latitud: number | null;
    longitud: number | null;
}

export interface DestinoUpsertDTO {
    id: number;
    nombre: string;
    pais: string;
    ciudad: string;
    latitud: number | null;
    longitud: number | null;
}
