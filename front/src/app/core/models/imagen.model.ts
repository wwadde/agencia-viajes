export interface ImagenSubidaResponse {
    referencia: string;
    url: string;
}

export interface ImagenBibliotecaItemDTO {
    referencia: string;
    url: string;
    carpeta: string;
    nombre: string;
    tamanoBytes: number;
    fechaModificacion: string;
}
