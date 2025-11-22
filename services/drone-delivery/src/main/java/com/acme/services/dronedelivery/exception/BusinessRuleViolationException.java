package com.acme.services.dronedelivery.exception;

/** Base exception for business rule violations. Maps to HTTP 400. */
public class BusinessRuleViolationException extends RuntimeException {

  public BusinessRuleViolationException(String message) {
    super(message);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
