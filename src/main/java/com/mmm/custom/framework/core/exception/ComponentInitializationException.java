package com.mmm.custom.framework.core.exception;

public class ComponentInitializationException extends Exception {

    public ComponentInitializationException(String message) {
        super(message);
    }

    public ComponentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
