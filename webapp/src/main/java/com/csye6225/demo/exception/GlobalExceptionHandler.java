package com.csye6225.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = DataValidationException.class)
    public ResponseEntity<DataValidationException> errorHandler(Exception ex) {
        DataValidationException error = new DataValidationException();
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
