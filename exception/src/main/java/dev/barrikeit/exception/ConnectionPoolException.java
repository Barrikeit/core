package dev.barrikeit.exception;

public class ConnectionPoolException extends BaseException {

  public ConnectionPoolException(String message) {
    super(message);
  }

  public ConnectionPoolException(String message, Object... values) {
    super(String.format(message, values));
  }

  public ConnectionPoolException(String message, Throwable cause) {
    super(message, cause);
  }
}
