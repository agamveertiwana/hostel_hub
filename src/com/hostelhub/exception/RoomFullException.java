package com.hostelhub.exception;

/**
 * Thrown when trying to allocate a student to a room that has already reached maximum capacity.
 */
public class RoomFullException extends HostelException {
    private static final long serialVersionUID = 1L;

    public RoomFullException(String roomId, int capacity) {
        super(String.format("Room '%s' is full! Capacity limit (%d) reached.", roomId, capacity));
    }
}
