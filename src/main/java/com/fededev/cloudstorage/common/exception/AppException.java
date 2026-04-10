package com.fededev.cloudstorage.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.status = errorCode.getStatus();
    }
}
