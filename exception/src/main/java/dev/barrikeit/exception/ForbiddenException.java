package dev.barrikeit.exception;

/** Thrown when an authenticated user lacks the required authority for a resource (HTTP 403). */
public class ForbiddenException extends BaseException {

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Object... args) {
    super(message, args);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }
}
