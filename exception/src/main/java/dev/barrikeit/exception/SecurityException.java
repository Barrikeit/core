package dev.barrikeit.exception;

public class SecurityException extends BaseException {

  public SecurityException(String message) {
    super(message);
  }

  public SecurityException(String message, Object... values) {
    super(String.format(message, values));
  }

  public SecurityException(String message, Throwable cause) {
    super(message, cause);
  }
}
