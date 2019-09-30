package com.csye6225.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<DataValidationException> handle(WebExchangeBindException ex) {
        //获取参数校验错误集合
        List<FieldError> fieldErrors = ex.getFieldErrors();
        //格式化以提供友好的错误提示
        String data = String.format("参数校验错误（%s）：%s", fieldErrors.size(),
                fieldErrors.stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.joining(";")));

        DataValidationException error = new DataValidationException();
        error.setStatus(400);
        error.setError("Bad Request");
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }
}