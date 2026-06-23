package dev.barrikeit.exception;

import java.io.Serial;

public class SecurityException extends BaseException {
  @Serial private static final long serialVersionUID = 1L;

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
