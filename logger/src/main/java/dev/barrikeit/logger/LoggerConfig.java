package dev.barrikeit.logger;

import dev.barrikeit.runtime.ConfigFileReader;
import dev.barrikeit.runtime.RunLevel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LoggerConfig {

  private static final String[] CONFIG_FILE_CANDIDATES = {
    "logger.yml", "logger.yaml", "logger.properties"
  };

  private LoggerConfig() {}

  public static void configure() {
    Map<String, String> props = loadConfigFile();
    Level level = LogLevel.parse(props.getOrDefault("logger.level", "DEBUG"));
    applyConfig(level, props);
  }

  public static void configure(Level level) {
    applyConfig(level, Map.of());
  }

  // -------------------------------------------------------------------------

  private static void applyConfig(Level level, Map<String, String> props) {
    Logger rootLogger = LogManager.getLogManager().getLogger("");

    // Remove all existing handlers
    for (var handler : rootLogger.getHandlers()) {
      rootLogger.removeHandler(handler);
    }

    // Build formatter from config
    LoggerFormatter formatter = buildFormatter(props);

    boolean consoleEnabled = Boolean.parseBoolean(props.getOrDefault("logger.console", "true"));
    if (consoleEnabled) {
      ConsoleHandler consoleHandler = new ConsoleHandler();
      consoleHandler.setFormatter(formatter);
      consoleHandler.setLevel(level);
      rootLogger.addHandler(consoleHandler);
    }

    rootLogger.setLevel(level);

    installUncaughtExceptionHandler();
  }

  private static LoggerFormatter buildFormatter(Map<String, String> props) {
    LoggerFormatter formatter = new LoggerFormatter();

    String datePattern = props.get("logger.format.date");
    if (datePattern != null && !datePattern.isBlank()) {
      try {
        formatter.setDateFormat(DateTimeFormatter.ofPattern(datePattern));
      } catch (IllegalArgumentException e) {
        // invalid pattern — keep default silently
      }
    }

    String widthRaw = props.get("logger.format.logger.width");
    if (widthRaw != null) {
      try {
        formatter.setLoggerNameWidth(Integer.parseInt(widthRaw.trim()));
      } catch (NumberFormatException e) {
        // keep default
      }
    }

    return formatter;
  }

  private static Map<String, String> loadConfigFile() {
    for (String candidate : CONFIG_FILE_CANDIDATES) {
      try {
        InputStream stream = RunLevel.get(candidate);
        if (stream != null) {
          try (stream) {
            Map<String, String> props = ConfigFileReader.read(stream, candidate);
            if (!props.isEmpty()) return props;
          }
        }
      } catch (NoSuchFileException ignored) {
        // not found — try next candidate
      } catch (IOException e) {
        // found but unreadable — try next candidate
      }
    }
    return Map.of(); // no config file found, all defaults apply
  }

  private static void installUncaughtExceptionHandler() {
    Thread.UncaughtExceptionHandler handler =
        (thread, throwable) -> {
          Logger logger = Logger.getLogger(thread.getName());

          String message =
              String.format(
                  "Uncaught exception in thread '%s' [id=%d, priority=%d, group=%s]",
                  thread.getName(),
                  thread.threadId(),
                  thread.getPriority(),
                  thread.getThreadGroup().getName());

          LogRecord logRecord = new LogRecord(LogLevel.FATAL, message);
          logRecord.setLoggerName(thread.getName());
          if (throwable != null) {
            logRecord.setThrown(throwable);
          }
          logger.log(logRecord);
        };

    Thread.setDefaultUncaughtExceptionHandler(handler);
    for (Thread t : Thread.getAllStackTraces().keySet()) {
      t.setUncaughtExceptionHandler(handler);
    }
  }
}
