package com.acme.services.dronedelivery.exception;

/** Exception thrown when a user attempts to access a resource they don't own. Maps to HTTP 403. */
public class UnauthorizedAccessException extends RuntimeException {

  public UnauthorizedAccessException(String message) {
    super(message);
  }

  public UnauthorizedAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
