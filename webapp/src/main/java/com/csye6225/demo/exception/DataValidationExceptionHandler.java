package com.csye6225.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class DataValidationExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map errorHandler(Exception ex) {
        Map map = new HashMap();
        map.put("code", 100);
        map.put("msg", ex.getMessage());
        return map;
    }

    @ResponseBody
    @ExceptionHandler(value = DataValidationException.class)
    public ResponseEntity DataValidationExceptionHandler(DataValidationException ex){
        Map map = new LinkedHashMap();
        map.put("timestamp",ex.getTimestamp());
        map.put("status",ex.getStatus());
        map.put("error",ex.getError());
        map.put("message",ex.getMessage());
        map.put("path",ex.getPath());
        switch(ex.getStatus()){
            case 400:
                return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
            case 401:
                return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
            case 404:
                return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
    }
}
