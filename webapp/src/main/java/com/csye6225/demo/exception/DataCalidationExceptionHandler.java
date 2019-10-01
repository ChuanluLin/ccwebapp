package com.csye6225.demo.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@ControllerAdvice
public class DataCalidationExceptionHandler {
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
    public Map DataValidationExceptionHandler(DataValidationException ex){
        Map map = new LinkedHashMap();
        map.put("timestamp",ex.getTimestamp());
        map.put("status",ex.getStatus());
        map.put("error",ex.getError());
        map.put("message",ex.getMessage());
        map.put("path",ex.getPath());
        return map;
    }
}
