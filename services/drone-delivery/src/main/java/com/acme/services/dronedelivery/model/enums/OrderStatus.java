package com.acme.services.dronedelivery.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
  PENDING,
  RESERVED,
  PICKED_UP,
  IN_TRANSIT,
  DELIVERED,
  FAILED,
  WITHDRAWN;

  @JsonValue
  public String toValue() {
    return this.name().toLowerCase();
  }

  @JsonCreator
  public static OrderStatus fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (OrderStatus status : OrderStatus.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
  }
}
