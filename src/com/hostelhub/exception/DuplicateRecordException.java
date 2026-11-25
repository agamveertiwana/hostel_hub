package com.hostelhub.exception;

/**
 * Thrown when attempting to add an entity whose unique key already exists.
 */
public class DuplicateRecordException extends HostelException {
    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String entityType, String identifier) {
        super(String.format("%s with identifier '%s' already exists!", entityType, identifier));
    }
}
