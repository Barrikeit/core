package dev.barrikeit.exception;

public class LoggerException extends BaseException {
  public LoggerException(String message) {
    super(message);
  }

  public LoggerException(String message, Object... values) {
    super(message, values);
  }

  public LoggerException(String message, Throwable cause) {
    super(message, cause);
  }
}
