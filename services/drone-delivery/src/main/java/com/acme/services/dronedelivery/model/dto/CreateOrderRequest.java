package com.acme.services.dronedelivery.model.dto;

import com.acme.services.dronedelivery.validation.ValidCoordinates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidCoordinates
@Schema(description = "Request to create a new delivery order")
public class CreateOrderRequest {

  @NotNull(message = "Origin latitude is required")
  @DecimalMin(value = "-90.0", message = "Origin latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Origin latitude must be between -90 and 90")
  @Digits(
      integer = 2,
      fraction = 8,
      message = "Origin latitude must have at most 2 integer digits and 8 decimal places")
  @Schema(
      description = "Pickup location latitude (range: -90 to 90)",
      example = "40.7128",
      minimum = "-90",
      maximum = "90")
  private BigDecimal originLatitude;

  @NotNull(message = "Origin longitude is required")
  @DecimalMin(value = "-180.0", message = "Origin longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Origin longitude must be between -180 and 180")
  @Digits(
      integer = 3,
      fraction = 8,
      message = "Origin longitude must have at most 3 integer digits and 8 decimal places")
  @Schema(
      description = "Pickup location longitude (range: -180 to 180)",
      example = "-74.0060",
      minimum = "-180",
      maximum = "180")
  private BigDecimal originLongitude;

  @NotNull(message = "Destination latitude is required")
  @DecimalMin(value = "-90.0", message = "Destination latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Destination latitude must be between -90 and 90")
  @Digits(
      integer = 2,
      fraction = 8,
      message = "Destination latitude must have at most 2 integer digits and 8 decimal places")
  @Schema(
      description = "Delivery location latitude (range: -90 to 90)",
      example = "40.7589",
      minimum = "-90",
      maximum = "90")
  private BigDecimal destinationLatitude;

  @NotNull(message = "Destination longitude is required")
  @DecimalMin(value = "-180.0", message = "Destination longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Destination longitude must be between -180 and 180")
  @Digits(
      integer = 3,
      fraction = 8,
      message = "Destination longitude must have at most 3 integer digits and 8 decimal places")
  @Schema(
      description = "Delivery location longitude (range: -180 to 180)",
      example = "-73.9851",
      minimum = "-180",
      maximum = "180")
  private BigDecimal destinationLongitude;
}
