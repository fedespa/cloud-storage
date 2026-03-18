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
    REFRESH_TOKEN_REUSE_DETECTED("ID_004", "Reutilización de refresh token detectada, sesiones revocadas", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

}
