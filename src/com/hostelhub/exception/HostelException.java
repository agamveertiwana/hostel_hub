package com.hostelhub.exception;

/**
 * Base checked exception for all domain-specific errors in HostelHub.
 */
public class HostelException extends Exception {
    private static final long serialVersionUID = 1L;

    public HostelException(String message) {
        super(message);
    }

    public HostelException(String message, Throwable cause) {
        super(message, cause);
    }
}
