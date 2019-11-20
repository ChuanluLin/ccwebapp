package com.csye6225.demo.exception;

public class RequestLimitException extends Exception {
    private static final long serialVersionUID = 1364225358754654702L;

    public RequestLimitException() {
        super("HTTP request limited");
    }

    public RequestLimitException(String message) {
        super(message);
    }
}
