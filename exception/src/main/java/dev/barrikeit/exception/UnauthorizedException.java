package dev.barrikeit.exception;

import java.io.Serial;

/** Thrown when a request lacks valid authentication credentials (HTTP 401). */
public class UnauthorizedException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Object... args) {
    super(message, args);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
