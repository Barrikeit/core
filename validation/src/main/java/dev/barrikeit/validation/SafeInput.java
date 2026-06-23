package dev.barrikeit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a field contains only safe characters: Unicode letters, digits, and {@code
 * @#$%^&+=}. Rejects HTML/SQL injection characters such as {@code < > ' " ; --}.
 */
@Documented
@Constraint(validatedBy = SafeInputValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeInput {
  String message() default "{validation.safe-input.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
