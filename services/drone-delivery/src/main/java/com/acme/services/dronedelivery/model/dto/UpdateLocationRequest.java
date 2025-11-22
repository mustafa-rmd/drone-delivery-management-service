package com.acme.services.dronedelivery.model.dto;

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
@Schema(description = "Request to update location coordinates")
public class UpdateLocationRequest {

  @NotNull(message = "Latitude is required")
  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  @Digits(
      integer = 2,
      fraction = 8,
      message = "Latitude must have at most 2 integer digits and 8 decimal places")
  @Schema(
      description = "Current latitude (range: -90 to 90)",
      example = "40.7128",
      minimum = "-90",
      maximum = "90")
  private BigDecimal latitude;

  @NotNull(message = "Longitude is required")
  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  @Digits(
      integer = 3,
      fraction = 8,
      message = "Longitude must have at most 3 integer digits and 8 decimal places")
  @Schema(
      description = "Current longitude (range: -180 to 180)",
      example = "-74.0060",
      minimum = "-180",
      maximum = "180")
  private BigDecimal longitude;
}
