export type PagoEstado =
    | 'PENDIENTE'
    | 'REQUIERE_ACCION'
    | 'APROBADO'
    | 'RECHAZADO'
    | 'CANCELADO'
    | 'EXPIRADO';

export type ProveedorPago =
    | 'SIN_CONFIGURAR'
    | 'MERCADOPAGO'
    | 'STRIPE'
    | 'PAYU';

export interface PagoIntentoDTO {
    pagoId: number;
    estado: PagoEstado;
    proveedor: ProveedorPago;
    referenciaExterna: string;
    urlCheckout: string;
    expiraEn: string;
    checkoutPayload: string;
}

export interface PagoCrearIntentoDTO {
    moneda?: string;
    idempotencyKey?: string;
}

export interface PagoResumenDTO {
    id: number;
    reservaId: number;
    estado: PagoEstado;
    proveedor: ProveedorPago;
    monto: number;
    moneda: string;
    referenciaExterna: string;
    urlCheckout: string;
    creadoEn: string;
    confirmadoEn?: string;
    expiraEn?: string;
}
