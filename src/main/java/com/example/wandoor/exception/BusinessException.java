package com.example.wandoor.exception;

import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus;

import lombok.Getter;



@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(HttpStatus status, String message ) {
        super(message);
        this.status = status;
        this.errorCode = status.name();
    }

    public BusinessException(HttpStatus status, String errorCode, String message){
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public BusinessException(HttpStatus status, String errorCode, String message, Throwable cause){
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }


}
