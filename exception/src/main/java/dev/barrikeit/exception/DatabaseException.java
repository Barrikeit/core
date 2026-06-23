package dev.barrikeit.exception;

import java.io.Serial;

public class DatabaseException extends BaseException {
  @Serial private static final long serialVersionUID = 1L;

  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Object... values) {
    super(String.format(message, values));
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
