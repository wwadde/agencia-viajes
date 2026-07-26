export interface ChatRequest {
  peticion: string;
  conversacionId?: number | null;
}

export interface ChatMessage {
  id: number | string;
  role: 'USUARIO' | 'CHATBOT';
  content: string;
}


export interface ChatEvent {
  type: string;
  data: string;
}

export interface ConversacionResponse {
  id: number;
  titulo: string;
  fechaCreacion: string;
  fechaActualizacion: string;
  mensajes: MensajeResponse[];
}

export interface MensajeResponse {
  id: number;
  mensaje: string;
  rolMensaje: 'USUARIO' | 'CHATBOT';
  fechaHora: string;
}