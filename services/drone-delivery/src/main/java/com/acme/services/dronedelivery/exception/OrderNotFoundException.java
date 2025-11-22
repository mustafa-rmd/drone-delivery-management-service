package com.acme.services.dronedelivery.exception;

/** Exception thrown when an order is not found in the system. */
public class OrderNotFoundException extends ResourceNotFoundException {

  public OrderNotFoundException(Long orderId) {
    super("Order not found: " + orderId);
  }

  public OrderNotFoundException(String message) {
    super(message);
  }
}
