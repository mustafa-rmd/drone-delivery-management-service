package com.acme.services.dronedelivery.exception;

/** Exception thrown when an order operation is attempted in an invalid state. */
public class InvalidOrderStateException extends BusinessRuleViolationException {

  public InvalidOrderStateException(String message) {
    super(message);
  }
}
