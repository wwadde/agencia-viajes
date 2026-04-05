package com.agencia.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejador personalizado para peticiones autenticadas sin los permisos necesarios.
 * Devuelve un cuerpo RFC 7807 (Problem Details).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemDetailWriter problemDetailWriter;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("Acceso denegado a: {} para el usuario: {}",
                request.getRequestURI(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "desconocido");
        problemDetailWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "No tiene permisos para acceder a este recurso",
                "https://httpstatuses.io/403"
        );
    }
}

