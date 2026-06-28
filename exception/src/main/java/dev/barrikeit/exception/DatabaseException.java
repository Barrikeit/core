package dev.barrikeit.exception;

public class DatabaseException extends BaseException {

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
