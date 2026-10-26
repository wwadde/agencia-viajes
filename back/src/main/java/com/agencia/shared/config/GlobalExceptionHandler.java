package com.agencia.shared.config;

import com.agencia.auth.exception.UsuarioYaExisteException;
import com.agencia.chatbot.exception.ChatbotException;
import com.agencia.chatbot.exception.ConversacionException;
import com.agencia.imagen.exception.AlmacenamientoException;
import com.agencia.imagen.exception.ArchivoInvalidoException;
import com.agencia.imagen.exception.ArchivoNoEncontradoException;
import com.agencia.imagen.exception.CarpetaInvalidaException;
import com.agencia.shared.exception.RecursoNoEncontradoException;
import com.agencia.shared.exception.ReglaNegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Manejador global de excepciones que devuelve respuestas RFC 7807 (Problem Details).
 *
 * <p>Todos los handlers devuelven {@link ResponseEntity}{@code <ProblemDetail>} para
 * garantizar que Spring establezca correctamente el código HTTP en el response,
 * con {@code Content-Type: application/problem+json}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String VIOLATIONS_FIELD = "violations";
    private static final String URI_400 = "https://httpstatuses.io/400";
    private static final String URI_401 = "https://httpstatuses.io/401";
    private static final String URI_403 = "https://httpstatuses.io/403";
    private static final String URI_404 = "https://httpstatuses.io/404";
    private static final String URI_409 = "https://httpstatuses.io/409";
    private static final String URI_500 = "https://httpstatuses.io/500";


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(
            @SuppressWarnings("unused") BadCredentialsException ex) {
        log.warn("Intento de login con credenciales incorrectas");
        return problem(HttpStatus.UNAUTHORIZED, "Autenticación fallida", "Credenciales incorrectas", URI_401);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetail> handleDisabled(
            @SuppressWarnings("unused") DisabledException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Cuenta deshabilitada",
                "La cuenta esta deshabilitada. Contacte al administrador.", URI_401);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(
            @SuppressWarnings("unused") LockedException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Cuenta bloqueada",
                "La cuenta esta bloqueada. Contacte al administrador.", URI_401);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> handleLocked(
            @SuppressWarnings("unused") AuthorizationDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Sin permisos",
                "No tiene permisos para acceder al recurso", URI_403);
    }


    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<ProblemDetail> handleUsuarioYaExiste(UsuarioYaExisteException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto de recurso", ex.getMessage(), URI_409);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Conflicto de recurso",
                "No fue posible guardar el recurso por restricción de datos", URI_409);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), URI_404);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ProblemDetail> handleReglaNegocio(ReglaNegocioException ex) {
        return problem(HttpStatus.CONFLICT, "Regla de negocio", ex.getMessage(), URI_409);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "campo", fe.getField(),
                        "mensaje", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor invalido"
                ))
                .toList();

        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos",
                "La solicitud contiene %d campo(s) invalido(s)".formatted(violations.size()), URI_400);
        pd.setProperty(VIOLATIONS_FIELD, violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Tamano de archivo excedido: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Archivo demasiado grande",
                "El archivo supera el tamaño máximo permitido", URI_400);
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    public ResponseEntity<ProblemDetail> handleArchivoInvalido(ArchivoInvalidoException ex) {
        log.warn("Archivo invalido: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Archivo inválido", ex.getMessage(), URI_400);
    }

    @ExceptionHandler(CarpetaInvalidaException.class)
    public ResponseEntity<ProblemDetail> handleArchivoInvalido(CarpetaInvalidaException ex) {
        log.warn("Carpeta invalida: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Carpeta inválida", ex.getMessage(), URI_400);
    }

    @ExceptionHandler(ArchivoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleArchivoNoEncontrado(ArchivoNoEncontradoException ex) {
        log.warn("Archivo no encontrado: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Archivo no encontrado", ex.getMessage(), URI_404);
    }

    @ExceptionHandler(AlmacenamientoException.class)
    public ResponseEntity<ProblemDetail> handleAlmacenamiento(AlmacenamientoException ex) {
        log.error("Error de almacenamiento: {}", ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error de almacenamiento",
                "Error al procesar el archivo. Por favor, intente nuevamente.", URI_500);
    }


    /** Red de seguridad para cualquier excepcion no manejada especificamente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Error interno no controlado: {}", ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                "Se produjo un error interno. Por favor, intente nuevamente.", URI_500);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(NoResourceFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado",
                "El recurso al que intenta acceder no existe o no esta disponible", URI_404);
    }

    @ExceptionHandler(ConversacionException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(ConversacionException ex) {
        log.warn("Conversación no encontrada: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Conversación no encontrada",
                ex.getMessage(), URI_404);
    }

    @ExceptionHandler(ChatbotException.class)
    public ResponseEntity<ProblemDetail> handleChatbotException(ChatbotException ex) {
        log.warn("Error de chatbot: {}", ex.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Error chat",
                ex.getMessage(), URI_403);
    }




    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status, String title, String detail, String typeUri) {
        return ResponseEntity.status(status).body(build(status, title, detail, typeUri));
    }

    private ProblemDetail build(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(URI.create(typeUri));
        pd.setProperty(TIMESTAMP_FIELD, LocalDateTime.now());
        return pd;
    }
}
