package dev.barrikeit.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SearchParamsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchParams {
    String message() default "El parámetro params no tiene el formato correcto.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}