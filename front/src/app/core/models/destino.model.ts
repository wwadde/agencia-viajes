export interface DestinoDTO {
    id: number;
    nombre: string;
    pais: string;
    ciudad: string;
}

export interface DestinoUpsertDTO {
    nombre: string;
    pais: string;
    ciudad: string;
}
