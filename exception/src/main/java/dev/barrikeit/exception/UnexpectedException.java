package dev.barrikeit.exception;

public class UnexpectedException extends BaseException {
  public UnexpectedException(String message) {
    super(message);
  }

  public UnexpectedException(String message, Object... values) {
    super(message, values);
  }

  public UnexpectedException(String message, Throwable cause) {
    super(message, cause);
  }
}
