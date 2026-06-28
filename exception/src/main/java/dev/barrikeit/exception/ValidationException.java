package dev.barrikeit.exception;

/** Thrown when input data fails business or constraint validation (maps to HTTP 400). */
public class ValidationException extends BaseException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Object... args) {
    super(message, args);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
