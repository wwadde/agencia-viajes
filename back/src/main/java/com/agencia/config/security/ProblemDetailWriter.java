package com.agencia.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

/**
 * Utilidad para serializar un {@link ProblemDetail} (RFC 7807) directamente
 * sobre un {@link HttpServletResponse}.
 *
 * <p>Se emplea en componentes que viven fuera del ciclo de vida del
 * {@code DispatcherServlet} (filtros, entry points, access-denied handlers)
 * donde {@code @RestControllerAdvice} no tiene alcance.
 */


@Component
@RequiredArgsConstructor
public class ProblemDetailWriter {

    private final ObjectMapper mapper;

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String title,
            String detail,
            String typeUri
    ) throws IOException {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(typeUri));
        // Se define la instancia manualmente porque no pasa por el flujo normal de MVC
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", LocalDateTime.now());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        mapper.writeValue(response.getWriter(), problem);
    }
}

