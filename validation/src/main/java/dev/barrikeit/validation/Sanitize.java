package dev.barrikeit.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation indicating that a field should be trimmed before processing.
 *
 * <p>This annotation does not perform constraint validation — it is a signal to the binding layer
 * or a custom {@code HandlerMethodArgumentResolver} / {@code Converter} to apply
 * {@link String#strip()} on the incoming value.
 *
 * <p>Pair with a {@code @ControllerAdvice} or a Jackson deserializer that honours this annotation.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sanitize {
}
