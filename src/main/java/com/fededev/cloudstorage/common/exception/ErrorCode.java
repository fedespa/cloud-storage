package com.fededev.cloudstorage.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EMAIL_ALREADY_EXISTS("ID_001", "El email ya está registrado", HttpStatus.CONFLICT),
    REFRESH_TOKEN_INVALID("ID_002", "El refresh token es inválido o fue revocado", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("ID_003", "El refresh token ha expirado", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSE_DETECTED("ID_004", "Reutilización de refresh token detectada, sesiones revocadas", HttpStatus.UNAUTHORIZED),

    // WORKSPACE ERRORS
    WORKSPACE_NOT_FOUND("W_001", "El workspace no existe", HttpStatus.NOT_FOUND),
    WORKSPACE_ACCESS_DENIED("W_002", "No tienes permisos suficientes en este workspace", HttpStatus.FORBIDDEN),
    WORKSPACE_MEMBER_NOT_FOUND("W_003", "El usuario no pertenece a este workspace", HttpStatus.FORBIDDEN),
    WORKSPACE_MEMBER_ALREADY_EXISTS("W_004", "El usuario ya es miembro de este workspace", HttpStatus.CONFLICT),
    WORKSPACE_OWNER_REQUIRED("W_005", "El workspace debe tener al menos un propietario", HttpStatus.BAD_REQUEST),
    WORKSPACE_INVITATION_ALREADY_SENT("W_006", "Ya existe una invitación pendiente para este usuario", HttpStatus.CONFLICT),
    WORKSPACE_LIMIT_REACHED("W_007", "Se alcanzó el límite de miembros del workspace", HttpStatus.BAD_REQUEST),
    WORKSPACE_INVALID_ROLE("W_008", "El rol asignado no es válido para este workspace", HttpStatus.BAD_REQUEST),
    WORKSPACE_QUOTA_EXCEEDED("W_009", "El workspace no tiene mas espacio", HttpStatus.CONFLICT),

    // INVITATION ERRORS
    INVITATION_NOT_FOUND("I_001", "Invitación no encontrada", HttpStatus.NOT_FOUND),
    INVITATION_ALREADY_ACCEPTED("I_002", "Invitación ya aceptada", HttpStatus.CONFLICT),
    INVITATION_EXPIRED("I_003", "Invitación expirada",  HttpStatus.BAD_REQUEST),

    // FOLDER ERRORS
    FOLDER_NOT_FOUND("F_001", "La carpeta no existe", HttpStatus.NOT_FOUND),
    FOLDER_ALREADY_EXISTS_IN_LEVEL("F_002", "Ya existe una carpeta con ese nombre en el mismo nivel", HttpStatus.CONFLICT),

    // FILE ERRORS
    FILE_NOT_FOUND("FI_001", "El archivo no existe", HttpStatus.NOT_FOUND),
    FILE_IS_EMPTY("FI_002", "El archivo está vacío", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FI_003", "El archivo excede el tamaño máximo permitido", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_TYPE("FI_004", "Tipo de archivo no permitido", HttpStatus.BAD_REQUEST),
    STORAGE_UPLOAD_ERROR("FI_005", "Error crítico al subir el archivo al proveedor de almacenamiento", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED("FI_006", "No se pudo completar la subida del archivo", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_EXTENSION("FI_007", "Tipo de extensión no permitida", HttpStatus.BAD_REQUEST),
    FILE_READ_ERROR("FI_008", "Error del servidor al leer el archivo", HttpStatus.INTERNAL_SERVER_ERROR),


    INTERNAL_SERVER_ERROR("G_001", "Ha ocurrido un error interno", HttpStatus.INTERNAL_SERVER_ERROR);
    private final String code;
    private final String message;
    private final HttpStatus status;

}
