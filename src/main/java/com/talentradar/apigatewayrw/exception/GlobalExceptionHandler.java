package com.talentradar.apigatewayrw.exception;

import com.talentradar.apigatewayrw.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException
    (Exception exception, WebRequest webRequest) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Unexpected Exception")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // handle session is not found exception response
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<?> handleSessionNotFound(
            SessionNotFoundException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("SessionId doesnt exist")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException
            (UnauthorizedException unauthorizedException) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Unauthorized Exception")
                .errors(List.of(Map.of("message", unauthorizedException.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // handle any unexpected Error
    @ExceptionHandler(Error.class)
    public ResponseEntity<?> handleError(Error error) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Internal Server Error")
                .errors(List.of(Map.of("message", error.getMessage())))
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
