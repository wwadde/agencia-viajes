package com.agencia.shared.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada personalizado para peticiones no autenticadas.
 * Devuelve un cuerpo RFC 7807 (Problem Details) en lugar del redirect por defecto.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemDetailWriter problemDetailWriter;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.warn("Acceso no autenticado a: {}", request.getRequestURI());
        problemDetailWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                "Debe autenticarse para acceder a este recurso",
                "https://httpstatuses.io/401"
        );
    }
}

