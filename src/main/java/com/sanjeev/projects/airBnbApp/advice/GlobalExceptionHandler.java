package com.sanjeev.projects.airBnbApp.advice;

import com.sanjeev.projects.airBnbApp.exceptions.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.JMException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException exception){
        ApiError apiError = ApiError.
                builder().
                status(HttpStatus.NOT_FOUND)
                .message(exception.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerError(Exception e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }
    @ExceptionHandler(JMException.class)
    public ResponseEntity<ApiResponse<?>> handleJwtException(JwtException e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.FORBIDDEN)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    public ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError apiError){
        return new ResponseEntity<>(new ApiResponse<>(apiError),apiError.getStatus());
    }
}
