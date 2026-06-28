package dev.barrikeit.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * Console / file log formatter.
 *
 * <p>Produces lines shaped like:
 *
 * <pre>
 * 2026-06-28 10:11:12.345 INFO  --- [main] [correlation-id] dev.barrikeit.Main - message
 * </pre>
 *
 * <p>Level names are normalised so that both the custom {@link LogLevel} names and the standard
 * {@code java.util.logging} names emitted by the SLF4J→JUL bridge (slf4j-jdk14: SEVERE / WARNING /
 * INFO / FINE / FINEST) render consistently as FATAL / ERROR / WARN / INFO / DEBUG / TRACE.
 *
 * <p>When {@code ansi} is false (file output) all colour escape codes are omitted.
 */
public class LoggerFormatter extends Formatter {

  private static final String FULL_RESET = "\u001B[0m";
  private static final String BG_RESET = "\u001B[49m";
  private static final String FG_RESET = "\u001B[39m";
  private static final String CUSTOM_RESET = "\u001B[38;5;250m";

  private static final String GREY = "\u001B[37m";
  private static final String BG_RED = "\u001B[41m\u001B[97m";
  private static final String RED = "\u001B[31m";
  private static final String YELLOW = "\u001B[33m";
  private static final String GREEN = "\u001B[32m";
  private static final String BLUE = "\u001B[34m";
  private static final String CYAN = "\u001B[36m";

  private DateTimeFormatter dateFormat;
  private int loggerNameWidth;
  private final boolean ansi;

  public LoggerFormatter() {
    this(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"), 40, true);
  }

  public LoggerFormatter(DateTimeFormatter dateFormat, int loggerNameWidth, boolean ansi) {
    this.dateFormat = dateFormat;
    this.loggerNameWidth = loggerNameWidth;
    this.ansi = ansi;
  }

  public void setDateFormat(DateTimeFormatter dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setLoggerNameWidth(int loggerNameWidth) {
    this.loggerNameWidth = loggerNameWidth;
  }

  /** Maps custom and standard JUL level names to a consistent display label. */
  private static String displayLevel(String levelName) {
    return switch (levelName) {
      case "FATAL" -> "FATAL";
      case "SEVERE", "ERROR" -> "ERROR";
      case "WARNING", "WARN" -> "WARN";
      case "INFO", "CONFIG" -> "INFO";
      case "FINE", "DEBUG" -> "DEBUG";
      case "FINER", "FINEST", "TRACE" -> "TRACE";
      default -> levelName;
    };
  }

  private String colorFor(String displayLevel) {
    return switch (displayLevel) {
      case "FATAL" -> BG_RED;
      case "ERROR" -> RED;
      case "WARN" -> YELLOW;
      case "INFO" -> GREEN;
      case "DEBUG" -> BLUE;
      default -> GREY;
    };
  }

  private String paint(String color, String text) {
    if (!ansi) {
      return text;
    }
    String reset = color.equals(BG_RED) ? BG_RESET + CUSTOM_RESET : CUSTOM_RESET;
    return color + text + reset;
  }

  @Override
  public String format(LogRecord record) {
    String display = displayLevel(record.getLevel().getName());

    String date = paint(BLUE, dateFormat.format(LocalDateTime.now()));
    String level = paint(colorFor(display), String.format("%-5s", display));
    String thread = Thread.currentThread().getName();
    String logger =
        paint(CYAN, String.format("%-" + loggerNameWidth + "s", record.getLoggerName()));
    String message = formatMessage(record);

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%s %s --- [%s] %s - %s%n", date, level, thread, logger, message));

    if (record.getThrown() != null) {
      StringWriter sw = new StringWriter();
      record.getThrown().printStackTrace(new PrintWriter(sw));
      String trace =
          sw.toString()
              .lines()
              .map(line -> "    " + line)
              .collect(Collectors.joining(System.lineSeparator()));
      if (ansi) {
        sb.append(RED).append(trace).append(System.lineSeparator()).append(FULL_RESET);
      } else {
        sb.append(trace).append(System.lineSeparator());
      }
    }

    return sb.toString();
  }
}
