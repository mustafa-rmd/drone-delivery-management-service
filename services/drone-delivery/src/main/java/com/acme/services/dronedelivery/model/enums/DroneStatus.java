package com.acme.services.dronedelivery.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DroneStatus {
  AVAILABLE,
  BUSY,
  BROKEN;

  @JsonValue
  public String toValue() {
    return this.name().toLowerCase();
  }

  @JsonCreator
  public static DroneStatus fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (DroneStatus status : DroneStatus.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DroneStatus value: " + value);
  }
}
