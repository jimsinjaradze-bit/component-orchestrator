package com.solveva.component_orchestrator.component;

import org.springframework.http.HttpStatus;

/**
 * Raised when a downstream component answers with a non-2xx status or cannot be reached.
 */
public class ComponentInvocationException extends RuntimeException {

    private final String component;
    private final HttpStatus status;

    public ComponentInvocationException(String component, HttpStatus status, String detail) {
        super("Component '" + component + "' failed with " + status + ": " + detail);
        this.component = component;
        this.status = status;
    }

    public String getComponent() {
        return component;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
