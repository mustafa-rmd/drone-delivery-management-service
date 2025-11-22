package com.acme.services.dronedelivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that origin and destination coordinates are not identical. This ensures that delivery
 * orders have meaningful routes.
 */
@Documented
@Constraint(validatedBy = ValidCoordinatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCoordinates {

  String message() default "Origin and destination coordinates must be different";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
