package dev.barrikeit.exception;

import java.io.Serial;

public class ConnectionPoolException extends BaseException {
  @Serial private static final long serialVersionUID = 1L;

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
