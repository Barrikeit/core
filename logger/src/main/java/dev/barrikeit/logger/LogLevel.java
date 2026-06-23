package dev.barrikeit.logger;

import java.util.logging.Level;

public class LogLevel extends Level {

  public static final Level FATAL = new LogLevel("FATAL", 1100); // same SEVERE
  public static final Level ERROR = new LogLevel("ERROR", 1000); // error
  public static final Level WARN = new LogLevel("WARN", 900); // same as WARNING
  public static final Level INFO = new LogLevel("INFO", 800); // same as INFO
  public static final Level DEBUG = new LogLevel("DEBUG", 600); // same as FINE

  protected LogLevel(String name, int value) {
    super(name, value);
  }

  public static Level parse(String name) {
    return switch (name.toUpperCase()) {
      case "FATAL" -> FATAL;
      case "ERROR" -> ERROR;
      case "WARN" -> WARN;
      case "DEBUG" -> DEBUG;
      default -> INFO;
    };
  }
}
