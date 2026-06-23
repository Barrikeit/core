package dev.barrikeit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a password meets the minimum strength requirements:
 *
 * <ul>
 *   <li>At least 8 characters
 *   <li>At least one digit
 *   <li>At least one uppercase letter
 *   <li>At least one lowercase letter
 *   <li>At least one special character from {@code @#$%^&+=}
 *   <li>No whitespace
 * </ul>
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
  String message() default "{validation.password.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
