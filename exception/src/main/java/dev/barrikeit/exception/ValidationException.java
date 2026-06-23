package dev.barrikeit.exception;

import java.io.Serial;

/** Thrown when input data fails business or constraint validation (maps to HTTP 400). */
public class ValidationException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

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
