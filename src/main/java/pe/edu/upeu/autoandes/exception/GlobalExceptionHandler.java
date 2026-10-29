package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return respuesta(
                HttpStatus.BAD_REQUEST, "Datos inválidos", request.getRequestURI(), campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRestricciones(
            ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(error ->
                campos.put(error.getPropertyPath().toString(), error.getMessage()));
        return respuesta(
                HttpStatus.BAD_REQUEST, "Datos inválidos", request.getRequestURI(), campos);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponseDTO> manejarSolicitudIncorrecta(
            Exception exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene datos inválidos o incompletos",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrado(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConflicto(
            ConflictException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDTO> manejarSolicitudInvalida(
            InvalidRequestException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarAutenticacion(
            AuthenticationException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos, o cuenta inactiva",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIntegridad(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> manejarRutaNoEncontrada(
            NoResourceFoundException exception, HttpServletRequest request) {
        return respuesta(
                HttpStatus.NOT_FOUND, "No existe la ruta solicitada", request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarErrorInterno(
            Exception exception, HttpServletRequest request) {
        LOGGER.error("Error interno procesando {} {}", request.getMethod(), request.getRequestURI(), exception);
        return respuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                request.getRequestURI(),
                Map.of());
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(
            HttpStatus status, String mensaje, String ruta, Map<String, String> campos) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                ruta,
                campos);
        return ResponseEntity.status(status).body(error);
    }
}
