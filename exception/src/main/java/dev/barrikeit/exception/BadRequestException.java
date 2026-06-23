package dev.barrikeit.exception;

import java.io.Serial;

/** Thrown when the client sends a malformed or semantically invalid request (HTTP 400). */
public class BadRequestException extends BaseException {

  @Serial private static final long serialVersionUID = 1L;

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Object... args) {
    super(message, args);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
