export type ForoBloqueTipo = 'TEXTO' | 'IMAGEN';

export interface ForoPostResumenDTO {
  id: number;
  titulo: string;
  slug: string;
  resumen: string;
  imagenPortada: string | null;
  fechaPublicacion: string | null;
  totalComentarios: number;
  totalLikes: number;
  likedByMe: boolean;
}

export interface ForoPostPageDTO {
  items: ForoPostResumenDTO[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface ForoBloqueDTO {
  id: number;
  tipo: ForoBloqueTipo;
  contenido: string;
  pieImagen: string | null;
  orden: number;
}

export interface ForoComentarioDTO {
  id: number;
  autor: string;
  contenido: string;
  fechaCreacion: string;
  oculto: boolean;
  totalLikes: number;
  likedByMe: boolean;
}

export interface ForoPostDetalleDTO {
  id: number;
  titulo: string;
  slug: string;
  resumen: string;
  publicado: boolean;
  fechaPublicacion: string | null;
  ultimaModificacion: string;
  autor: string | null;
  totalLikes: number;
  likedByMe: boolean;
  bloques: ForoBloqueDTO[];
  comentarios: ForoComentarioDTO[];
}

export interface ForoBloqueUpsertDTO {
  tipo: ForoBloqueTipo;
  contenido: string;
  pieImagen?: string | null;
  orden: number;
}

export interface ForoPostUpsertDTO {
  titulo: string;
  slug?: string;
  resumen: string;
  publicado: boolean;
  bloques: ForoBloqueUpsertDTO[];
}

export interface ForoComentarioCrearDTO {
  contenido: string;
}

export interface ForoComentarioModeracionDTO {
  oculto: boolean;
}

export interface ForoReaccionDTO {
  totalLikes: number;
  likedByMe: boolean;
}
