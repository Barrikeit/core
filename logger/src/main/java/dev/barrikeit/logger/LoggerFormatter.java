package dev.barrikeit.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

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

  public LoggerFormatter() {
    this(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"), 30);
  }

  public LoggerFormatter(DateTimeFormatter dateFormat, int loggerNameWidth) {
    this.dateFormat = dateFormat;
    this.loggerNameWidth = loggerNameWidth;
  }

  public void setDateFormat(DateTimeFormatter dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setLoggerNameWidth(int loggerNameWidth) {
    this.loggerNameWidth = loggerNameWidth;
  }

  private String getLevelString(LogRecord record) {
    String levelName = record.getLevel().getName();
    String color =
        switch (levelName) {
          case "SEVERE", "FATAL" -> BG_RED;
          case "ERROR" -> RED;
          case "WARN" -> YELLOW;
          case "INFO" -> GREEN;
          case "DEBUG" -> BLUE;
          default -> GREY;
        };
    return color + String.format("%-5s", levelName) + BG_RESET + CUSTOM_RESET;
  }

  @Override
  public String format(LogRecord record) {
    String date = BLUE + dateFormat.format(LocalDateTime.now()) + CUSTOM_RESET;
    String level = getLevelString(record);
    String thread = Thread.currentThread().getName();
    String logger =
        CYAN + String.format("%-" + loggerNameWidth + "s", record.getLoggerName()) + CUSTOM_RESET;
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
      sb.append(RED).append(trace).append(System.lineSeparator()).append(FULL_RESET);
    }

    return sb.toString();
  }
}
