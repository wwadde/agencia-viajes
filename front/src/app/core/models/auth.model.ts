export interface UserInfoDTO {
    id: number;
    usuario: string;
    correo: string;
    rol: 'ADMIN' | 'CLIENTE';
}

export interface LoginDTO {
    usuario?: string;
    password?: string;
}

export interface RegistroUsuarioDTO {
    usuario: string;
    correo: string;
    password: string;
}
