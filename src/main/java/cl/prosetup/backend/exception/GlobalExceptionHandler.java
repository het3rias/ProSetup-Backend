package cl.prosetup.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas JSON con el código HTTP correcto. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> responder(HttpStatus status, String mensaje, Object detalle) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("fecha", LocalDateTime.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);
        if (detalle != null) cuerpo.put("detalle", detalle);
        return ResponseEntity.status(status).body(cuerpo);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(NotFoundException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> peticionInvalida(BadRequestException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> conflicto(ConflictException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT,
                "No se puede completar la operación: el registro está relacionado con otros datos o está duplicado", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> errores.put(f.getField(), f.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> autenticacion(AuthenticationException e) {
        return responder(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o token no válido", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "Acceso denegado: no tienes permiso para este recurso", null);
    }
}
