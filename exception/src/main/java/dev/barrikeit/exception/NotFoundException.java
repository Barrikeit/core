package dev.barrikeit.exception;

public class NotFoundException extends BaseException {

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, Object... values) {
    super(message, values);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
