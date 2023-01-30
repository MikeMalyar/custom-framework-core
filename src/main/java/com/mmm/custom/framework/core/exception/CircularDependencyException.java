package com.mmm.custom.framework.core.exception;

public class CircularDependencyException extends ComponentInitializationException {

    public CircularDependencyException(String message) {
        super(message);
    }

    public CircularDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
