package com.acme.services.dronedelivery.exception;

/** Exception thrown when an order assignment validation fails. */
public class OrderAssignmentException extends BusinessRuleViolationException {

  public OrderAssignmentException(String message) {
    super(message);
  }
}
