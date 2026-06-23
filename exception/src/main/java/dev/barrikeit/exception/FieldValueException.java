package dev.barrikeit.exception;

public class FieldValueException extends BaseException {
  public FieldValueException(String message) {
    super(message);
  }

  public FieldValueException(String message, Object... values) {
    super(message, values);
  }

  public FieldValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
