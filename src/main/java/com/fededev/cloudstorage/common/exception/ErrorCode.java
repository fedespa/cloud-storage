package com.fededev.cloudstorage.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EMAIL_ALREADY_EXISTS("ID_002", "El email ya está registrado", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;

}
