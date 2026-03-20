package com.fededev.cloudstorage.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException ex) {
        ApiError error = new ApiError(
                ex.getCode(),
                ex.getMessage(),
                Instant.now(),
                null
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }

    // Maneja errores de validación de Spring (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                "Error en los campos enviados",
                Instant.now(),
                details
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        ApiError error = new ApiError(
                "ACCESS_DENIED",
                "Accesso denegado",
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        String message = "El body no ha sido enviado";

        System.out.println(ex.getCause());

        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType().isEnum()) {
                message = String.format(
                        "Valor inválido para el campo '%s'",
                        ife.getPath().get(0).getPropertyName()
                );
            }
        }

        ApiError error = new ApiError(
                "INVALID_BODY",
                message,
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        ApiError error = new ApiError(
                "UNSUPPORTED_MEDIA_TYPE",
                "El tipo de contenido enviado no está soportado",
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ApiError error = new ApiError(
                "BAD_CREDENTIALS",
                "Credenciales incorrectas",
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex) {
        ApiError error = new ApiError(
                "BAD_CREDENTIALS",
                "Credenciales incorrectas",
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);


    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        String message = String.format("El parámetro '%s' de tipo %s es obligatorio",
                ex.getParameterName(),
                ex.getParameterType());

        ApiError error = new ApiError(
                "MISSING_PARAMETER",
                message,
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Maneja cualquier otro error inesperado (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        log.error("Error no controlado: ", ex);
        ApiError error = new ApiError(
                "INTERNAL_SERVER_ERROR",
                "Ha ocurrido un error inesperado",
                Instant.now(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
