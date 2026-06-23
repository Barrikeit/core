package dev.barrikeit.runtime;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class Core {

  private Core() {}

  public static InputStream getFileAsStream(Path path) throws FileNotFoundException {
    return new FileInputStream(path.toFile());
  }

  public static InputStream getResourceAsStream(String name) {
    return Core.class.getClassLoader().getResourceAsStream(name);
  }

  public static ByteArrayOutputStream read(InputStream stream) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int length;
    while ((length = stream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result;
  }

  public static String readResource(String name) throws IOException {
    InputStream stream = getResourceAsStream(name);
    if (stream == null) return null;
    try (stream) {
      return read(stream).toString();
    }
  }

  public static String readFile(Path path) throws IOException {
    try (InputStream stream = getFileAsStream(path)) {
      return read(stream).toString();
    }
  }
}
