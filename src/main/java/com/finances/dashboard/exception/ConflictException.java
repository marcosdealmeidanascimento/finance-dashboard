package com.finances.dashboard.exception;

/** The request is valid but clashes with the current state (e.g. editing a paid payment, email in use). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

}
