export interface ChatRequest {
  peticion: string;
  conversacionId?: number | null;
}

export interface ChatMessage {
  id: string;
  role: 'USUARIO' | 'CHATBOT';
  content: string;
}


export interface ChatEvent {
  type: string;
  data: string;
}
