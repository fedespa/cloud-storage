package com.fededev.cloudstorage.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

        String message = String.format(
                "El parámetro '%s' debe ser de tipo %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido"
        );

        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            message = String.format("El parámetro '%s' debe ser un UUID válido (36 caracteres)", ex.getName());
        }

        ApiError error = new ApiError(
                "INVALID_PARAMETER_TYPE",
                message,
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = "Formato inválido";

        if (ex.getMessage() != null && ex.getMessage().contains("Invalid UUID")) {
            message = "El valor enviado debe ser un UUID válido (36 caracteres)";
        }

        ApiError error = new ApiError(
                "INVALID_PARAMETER",
                message,
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {

        String message = String.format("La parte '%s' es obligatoria en la solicitud", ex.getRequestPartName());

        ApiError error = new ApiError(
                "MISSING_PART",
                message,
                Instant.now(),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

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
            String fieldName = ife.getPath().isEmpty() ? "desconocido" :
                    ife.getPath().get(ife.getPath().size() - 1).getPropertyName();

            Class<?> targetType = ife.getTargetType();

            if (targetType.isEnum()) {
                message = String.format("Valor inválido para el campo '%s'", fieldName);
            } else if (targetType.equals(UUID.class)) {
                message = String.format("El campo '%s' debe ser un UUID válido (36 caracteres)", fieldName);
            } else {
                message = String.format("El formato del campo '%s' es incorrecto para el tipo %s",
                        fieldName, targetType.getSimpleName());
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootMsg = ex.getMostSpecificCause().getMessage();

        if (rootMsg != null && rootMsg.contains("unique_file_active_in_root")) {
            ApiError error = new ApiError(
                    "DUPLICATE_FILE_NAME",
                    "Ya existe un archivo con ese nombre en la raíz de este workspace.",
                    Instant.now(),
                    null
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        ApiError genericError = new ApiError(
                "DATA_INTEGRITY_ERROR",
                "Error de integridad de datos en la operación.",
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericError);
    }

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
