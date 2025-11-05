package com.example.wandoor.exception;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import com.example.wandoor.exception.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatus().value());
        body.put("errorCode", ex.getErrorCode());
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        body.put("traceId", MDC.get("traceId"));
        body.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(ex.getStatus().value()).body(body);
    }    
    
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
            String message = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + " " + error.getDefaultMessage())
                    .findFirst()
                    .orElse("Validation failed");
    
            log.warn("Validation error: {}", message);
    
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("errorCode", "INVALID_REQUEST_ARGUMENT");
            body.put("message", message);
            body.put("timestamp", Instant.now().toString());
            body.put("traceId", MDC.get("traceId"));
            body.put("requestId", MDC.get("requestId"));
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
        
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
            log.warn("ResponseStatusException: {}", ex.getReason());
            
            Map<String, Object> body = new HashMap<>();
            body.put("status", ex.getStatusCode().value());
            body.put("errorCode", ex.getStatusCode().toString());
            body.put("message", ex.getReason());
            body.put("timestamp", Instant.now().toString());
            body.put("traceId", MDC.get("traceId"));
            body.put("requestId", MDC.get("requestId"));;
            
            return ResponseEntity.status(ex.getStatusCode()).body(body);
        }
        
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
            log.error("Unexpected error occurred", ex);
            
            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("errorCode", "INTERNAL_SERVER_ERROR");
            body.put("message", "Something went wrong. Please contact support.");
            body.put("timestamp", Instant.now().toString());
            body.put("traceId", MDC.get("traceId"));
            body.put("requestId", MDC.get("requestId"));
            
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
                }
}