package dev.barrikeit.exception;

/** Thrown when a request conflicts with existing state — e.g. duplicate unique key (HTTP 409). */
public class ConflictException extends BaseException {

  public ConflictException(String message) {
    super(message);
  }

  public ConflictException(String message, Object... args) {
    super(message, args);
  }

  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
