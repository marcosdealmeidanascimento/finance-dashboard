package com.finances.dashboard.exception;

/** Mapped to 429. Thrown when a client exceeds a rate limit (e.g. repeated failed logins). */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }

}
