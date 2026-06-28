package dev.barrikeit.exception;

/**
 * Thrown when a reflective method/field lookup fails inside the generic framework. Extends {@link
 * BaseException} so it is rendered consistently by the global exception handler.
 */
public class NoSuchMethodException extends BaseException {

  public NoSuchMethodException(String message) {
    super(message);
  }

  public NoSuchMethodException(String message, Object... messageArgs) {
    super(message, messageArgs);
  }
}
