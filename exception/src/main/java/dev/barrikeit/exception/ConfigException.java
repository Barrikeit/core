package dev.barrikeit.exception;

public class ConfigException extends BaseException {

  public ConfigException(String message) {
    super(message);
  }

  public ConfigException(String message, Object... values) {
    super(message, values);
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
