package com.mmm.custom.framework.core.exception;

public class PropertyNotAccessibleException extends ComponentPostProcessException {

    public PropertyNotAccessibleException(String message) {
        super(message);
    }

    public PropertyNotAccessibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
