package com.acme.services.dronedelivery.exception;

/** Exception thrown when a drone operation is attempted in an invalid state. */
public class InvalidDroneStateException extends BusinessRuleViolationException {

  public InvalidDroneStateException(String message) {
    super(message);
  }
}
