package com.acme.services.dronedelivery.exception;

/** Exception thrown when a drone is not found in the system. */
public class DroneNotFoundException extends ResourceNotFoundException {

  public DroneNotFoundException(String droneName) {
    super("Drone not found: " + droneName);
  }

  public DroneNotFoundException(Long droneId) {
    super("Drone not found with ID: " + droneId);
  }
}
