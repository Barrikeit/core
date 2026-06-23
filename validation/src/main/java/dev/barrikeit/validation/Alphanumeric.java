package dev.barrikeit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Validates that a field contains only Unicode letters and digits (no special characters). */
@Documented
@Constraint(validatedBy = AlphanumericValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alphanumeric {
  String message() default "{validation.alphanumeric.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
