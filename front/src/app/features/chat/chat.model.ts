export interface ChatRequest {
  peticion: string;
  conversacionId?: string | null;
}

export type ChatMessageRole =
  | 'USUARIO'
  | 'CHATBOT'
  | 'TOOL'
  | 'ERROR';

export interface ChatMessage {
  id: number | string;
  role: ChatMessageRole;
  content: string;
}

export interface ChatEvent {
  type: string;
  data: string;
}

export interface ConversacionResumen {
  id: string;
  titulo: string;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface ConversacionResponse {
  id: string;
  titulo: string;
  fechaCreacion: string;
  fechaActualizacion: string;
  mensajes: MensajeResponse[];
}

export interface MensajeResponse {
  id: number;
  mensaje: string;
  rolMensaje: ChatMessageRole;
  fechaHora: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}