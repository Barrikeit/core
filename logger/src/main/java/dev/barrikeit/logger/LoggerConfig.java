package dev.barrikeit.logger;

import dev.barrikeit.runtime.ConfigFileReader;
import dev.barrikeit.runtime.RunLevel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Configures {@code java.util.logging} (JUL) — the single logging backend for the stack. SLF4J
 * (Spring, Hibernate, application {@code @Slf4j}) is routed here via the {@code slf4j-jdk14}
 * bridge.
 *
 * <p>Mirrors the previous log4j2 setup: a coloured console appender (stdout), a rolling file
 * appender, and per-logger levels. Driven by {@code logger.yml} / {@code logger.properties}:
 *
 * <pre>
 * logging:
 *   level: INFO                 # root level
 *   console: true
 *   file:
 *     enabled: true
 *     path: logs
 *     name: application
 *     max-size-mb: 200
 *     max-files: 30
 *   format:
 *     date: "yyyy-MM-dd HH:mm:ss.SSS"
 *     logger:
 *       width: 40
 *   levels:
 *     dev.barrikeit: DEBUG
 *     org.springframework: ERROR
 *     org.hibernate: ERROR
 *     com.zaxxer.hikari: ERROR
 * </pre>
 */
public final class LoggerConfig {

  private static final String[] CONFIG_FILE_CANDIDATES = {
    "logger.yml", "logger.yaml", "logger.properties"
  };
  private static final String LEVELS_PREFIX = "logging.levels.";

  private LoggerConfig() {}

  public static void configure() {
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    Map<String, String> props = loadConfigFile();
    Level level = toJulLevel(props.getOrDefault("logging.level", "INFO"));
    applyConfig(level, props);
  }

  public static void configure(Level level) {
    applyConfig(level, Map.of());
  }

  // -------------------------------------------------------------------------

  private static void applyConfig(Level rootLevel, Map<String, String> props) {
    Logger rootLogger = LogManager.getLogManager().getLogger("");

    for (var handler : rootLogger.getHandlers()) {
      rootLogger.removeHandler(handler);
    }

    // Console (stdout) — coloured.
    boolean consoleEnabled = Boolean.parseBoolean(props.getOrDefault("logging.console", "true"));
    if (consoleEnabled) {
      StreamHandler consoleHandler =
          new StreamHandler(System.out, buildFormatter(props, true)) {
            @Override
            public synchronized void publish(LogRecord record) {
              super.publish(record);
              flush();
            }
          };
      consoleHandler.setLevel(Level.ALL);
      rootLogger.addHandler(consoleHandler);
    }

    // Rolling file — plain (no ANSI).
    boolean fileEnabled = Boolean.parseBoolean(props.getOrDefault("logging.file.enabled", "false"));
    if (fileEnabled) {
      FileHandler fileHandler = buildFileHandler(props);
      if (fileHandler != null) {
        fileHandler.setFormatter(buildFormatter(props, false));
        fileHandler.setLevel(Level.ALL);
        rootLogger.addHandler(fileHandler);
      }
    }

    // Root level + per-logger levels. Handlers are at ALL, so logger levels do the filtering.
    rootLogger.setLevel(rootLevel);
    applyLoggerLevels(props);

    installUncaughtExceptionHandler();
  }

  private static void applyLoggerLevels(Map<String, String> props) {
    for (Map.Entry<String, String> entry : props.entrySet()) {
      if (entry.getKey().startsWith(LEVELS_PREFIX)) {
        String loggerName = entry.getKey().substring(LEVELS_PREFIX.length());
        Logger.getLogger(loggerName).setLevel(toJulLevel(entry.getValue()));
      }
    }
  }

  /**
   * Maps a configured level name to the standard JUL level used for filtering, aligned with the
   * slf4j-jdk14 mapping (ERROR→SEVERE, WARN→WARNING, INFO→INFO, DEBUG→FINE, TRACE→FINEST).
   */
  private static Level toJulLevel(String name) {
    return switch (name == null ? "INFO" : name.trim().toUpperCase()) {
      case "OFF" -> Level.OFF;
      case "FATAL", "ERROR" -> Level.SEVERE;
      case "WARN", "WARNING" -> Level.WARNING;
      case "DEBUG" -> Level.FINE;
      case "TRACE" -> Level.FINEST;
      case "ALL" -> Level.ALL;
      default -> Level.INFO;
    };
  }

  private static LoggerFormatter buildFormatter(Map<String, String> props, boolean ansi) {
    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    String datePattern = props.get("logging.format.date");
    if (datePattern != null && !datePattern.isBlank()) {
      try {
        dateFormat = DateTimeFormatter.ofPattern(datePattern);
      } catch (IllegalArgumentException ignored) {
        // keep default
      }
    }

    int width = 40;
    String widthRaw = props.get("logging.format.logger.width");
    if (widthRaw != null) {
      try {
        width = Integer.parseInt(widthRaw.trim());
      } catch (NumberFormatException ignored) {
        // keep default
      }
    }

    return new LoggerFormatter(dateFormat, width, ansi);
  }

  private static FileHandler buildFileHandler(Map<String, String> props) {
    try {
      String dir = props.getOrDefault("logging.file.path", "logs");
      Files.createDirectories(Path.of(dir));

      String name = props.getOrDefault("logging.file.name", "application");
      int maxSizeMb = parseInt(props.get("logging.file.max-size-mb"), 200);
      int maxFiles = parseInt(props.get("logging.file.max-files"), 30);

      String pattern = dir + "/" + name + "-%g.log";
      // limit (bytes) triggers rotation; count is the number of rotated files kept; append=true.
      return new FileHandler(pattern, maxSizeMb * 1024 * 1024, Math.max(1, maxFiles), true);
    } catch (IOException e) {
      System.err.println("[LoggerConfig] Could not initialise file logging: " + e.getMessage());
      return null;
    }
  }

  private static int parseInt(String value, int fallback) {
    if (value == null) return fallback;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /**
   * Loads the logging config, merging every matching file found on the classpath so a shared base
   * (e.g. {@code logger.yaml} packaged in a dependency) can be overridden by an application's own
   * {@code logger.yaml}.
   *
   * <p>Merge precedence follows classpath order: the first occurrence of a key wins. Standard
   * Maven / Spring-Boot classpaths place the application's own resources before its dependencies, so
   * the app's {@code logger.yaml} overrides the base while the base fills in everything the app
   * doesn't set. Only the first candidate format that exists is used (yaml/yml/properties are not
   * mixed).
   */
  private static Map<String, String> loadConfigFile() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = LoggerConfig.class.getClassLoader();
    }

    for (String candidate : CONFIG_FILE_CANDIDATES) {
      try {
        List<URL> urls = Collections.list(cl.getResources(candidate));
        if (urls.isEmpty()) {
          continue;
        }
        Map<String, String> merged = new LinkedHashMap<>();
        for (URL url : urls) {
          try (InputStream stream = url.openStream()) {
            ConfigFileReader.read(stream, candidate).forEach(merged::putIfAbsent);
          } catch (IOException ignored) {
            // skip unreadable layer, keep merging the rest
          }
        }
        if (!merged.isEmpty()) {
          return merged;
        }
      } catch (IOException ignored) {
        // problem enumerating this candidate — try the next
      }
    }

    // Fallback: working-directory / non-classpath lookup (plain-Java runs from source).
    for (String candidate : CONFIG_FILE_CANDIDATES) {
      try (InputStream stream = RunLevel.get(candidate)) {
        if (stream != null) {
          Map<String, String> props = ConfigFileReader.read(stream, candidate);
          if (!props.isEmpty()) {
            return props;
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
