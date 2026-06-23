package dev.barrikeit.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum RunLevel {
  JAR,
  FILE,
  UNKNOWN;

  private static final RunLevel _LEVEL;

  static {
    URL url = RunLevel.class.getResource("RunLevel.class");
    if (url == null) {
      _LEVEL = RunLevel.UNKNOWN;
    } else {
      String plain = url.toString();
      _LEVEL = plain.startsWith("jar") ? JAR : plain.startsWith("file") ? FILE : UNKNOWN;
    }
  }

  public static RunLevel getLevel(Class<?> klass) {
    String resourceName = klass.getSimpleName() + ".class";
    URL url = klass.getResource(resourceName);
    if (url == null) return UNKNOWN;
    String plain = url.toString();
    return plain.startsWith("jar") ? JAR : plain.startsWith("file") ? FILE : UNKNOWN;
  }

  public static RunLevel getLevel() {
    return _LEVEL;
  }

  public static InputStream get(String file) throws IOException {
    return get(file, _LEVEL);
  }

  public static InputStream get(String file, RunLevel level) throws IOException {
    return switch (level) {
      case JAR -> {
        InputStream stream = RunLevel.class.getResourceAsStream("/" + file);
        if (stream != null) yield stream;
        yield get(file, FILE);
      }
      case FILE -> {
        Path workDir = Paths.get(System.getProperty("user.dir"));
        Path direct = workDir.resolve(file);
        Path resources = workDir.resolve("src").resolve("main").resolve("resources").resolve(file);
        if (direct.toFile().exists()) {
          yield Files.newInputStream(direct);
        } else if (resources.toFile().exists()) {
          yield Files.newInputStream(resources);
        } else {
          throw new NoSuchFileException(file);
        }
      }
      default -> new ByteArrayInputStream(new byte[0]);
    };
  }
}
