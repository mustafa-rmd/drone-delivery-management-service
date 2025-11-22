package com.acme.services.dronedelivery.validation;

import com.acme.services.dronedelivery.model.dto.CreateOrderRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Validator implementation for {@link ValidCoordinates} annotation. Ensures that origin and
 * destination coordinates are not identical.
 */
public class ValidCoordinatesValidator
    implements ConstraintValidator<ValidCoordinates, CreateOrderRequest> {

  private static final BigDecimal EPSILON = new BigDecimal("0.00000001"); // ~1.1mm precision

  @Override
  public void initialize(ValidCoordinates constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(CreateOrderRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true; // Null check is handled by @NotNull on individual fields
    }

    BigDecimal originLat = request.getOriginLatitude();
    BigDecimal originLon = request.getOriginLongitude();
    BigDecimal destLat = request.getDestinationLatitude();
    BigDecimal destLon = request.getDestinationLongitude();

    // If any coordinate is null, let @NotNull handle it
    if (originLat == null || originLon == null || destLat == null || destLon == null) {
      return true;
    }

    // Check if coordinates are effectively the same (within epsilon tolerance)
    boolean latitudesSame = originLat.subtract(destLat).abs().compareTo(EPSILON) < 0;
    boolean longitudesSame = originLon.subtract(destLon).abs().compareTo(EPSILON) < 0;

    if (latitudesSame && longitudesSame) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Origin and destination coordinates must be different (minimum distance: ~1 meter)")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
