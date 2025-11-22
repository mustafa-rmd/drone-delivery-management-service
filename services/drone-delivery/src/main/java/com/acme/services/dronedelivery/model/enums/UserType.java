package com.acme.services.dronedelivery.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
  ADMIN,
  ENDUSER,
  DRONE;

  @JsonValue
  public String toValue() {
    return this.name().toLowerCase();
  }

  @JsonCreator
  public static UserType fromValue(String value) {
    if (value == null) {
      return null;
    }
    for (UserType type : UserType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid UserType value: " + value);
  }
}
