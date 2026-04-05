export interface Violation {
    campo: string;
    mensaje: string;
}

export interface ProblemDetail {
    type: string;
    title: string;
    status: number;
    detail: string;
    timestamp: string;
    violations?: Violation[];
}
