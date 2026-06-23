package dev.barrikeit.runtime;

import dev.barrikeit.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigSource {

  private static final Logger log = Logger.getLogger(ConfigSource.class.getName());

  private final Map<String, String> data;

  private ConfigSource(Map<String, String> data) {
    this.data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
  }

  public static ConfigSource fromRunLevel(String fileName) {
    try {
      InputStream stream = RunLevel.get(fileName);
      try (stream) {
        return new ConfigSource(ConfigFileReader.read(stream, fileName));
      }
    } catch (NoSuchFileException e) {
      log.warning("Config file not found: " + fileName);
      return empty();
    } catch (IOException e) {
      throw new ConfigException("Failed to load config file: %s — %s", fileName, e.getMessage());
    }
  }

  public static ConfigSource fromPath(Path path) {
    String fileName = path.getFileName().toString();
    try (InputStream stream = Core.getFileAsStream(path)) {
      return new ConfigSource(ConfigFileReader.read(stream, fileName));
    } catch (IOException e) {
      throw new ConfigException("Failed to load config file: %s — %s", path, e.getMessage());
    }
  }

  public static ConfigSource fromStream(InputStream stream, String fileName) {
    try {
      return new ConfigSource(ConfigFileReader.read(stream, fileName));
    } catch (IOException e) {
      throw new ConfigException(
          "Failed to read config stream for: %s — %s", fileName, e.getMessage());
    }
  }

  public static ConfigSource empty() {
    return new ConfigSource(Map.of());
  }

  public static ConfigSource merge(ConfigSource base, ConfigSource override) {
    Map<String, String> merged = new LinkedHashMap<>(base.data);
    merged.putAll(override.data);
    return new ConfigSource(merged);
  }

  // -------------------------------------------------------------------------

  public String get(String key) {
    return data.get(key);
  }

  public String get(String key, String defaultValue) {
    return data.getOrDefault(key, defaultValue);
  }

  public String require(String key) {
    String value = data.get(key);
    if (value == null) throw new ConfigException("Required config key missing: '%s'", key);
    return value;
  }

  public int getInt(String key, int defaultValue) {
    String value = data.get(key);
    if (value == null) return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      log.warning(
          "Config key '"
              + key
              + "' is not a valid integer: "
              + value
              + " — using default "
              + defaultValue);
      return defaultValue;
    }
  }

  public long getLong(String key, long defaultValue) {
    String value = data.get(key);
    if (value == null) return defaultValue;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      log.warning(
          "Config key '"
              + key
              + "' is not a valid long: "
              + value
              + " — using default "
              + defaultValue);
      return defaultValue;
    }
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    String value = data.get(key);
    if (value == null) return defaultValue;
    return Boolean.parseBoolean(value.trim());
  }

  public boolean containsKey(String key) {
    return data.containsKey(key);
  }

  public Map<String, String> asMap() {
    return data;
  }

  @Override
  public String toString() {
    return "ConfigSource" + data;
  }
}
