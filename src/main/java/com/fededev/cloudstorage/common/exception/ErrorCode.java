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

    // INVITATION ERROR
    INVITATION_NOT_FOUND("I_001", "Invitación no encontrada", HttpStatus.NOT_FOUND),
    INVITATION_ALREADY_ACCEPTED("I_002", "Invitación ya aceptada", HttpStatus.CONFLICT),
    INVITATION_EXPIRED("I_003", "Invitación expirada",  HttpStatus.BAD_REQUEST),;

    private final String code;
    private final String message;
    private final HttpStatus status;

}
