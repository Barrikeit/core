package dev.barrikeit.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ConfigFileReader {

  private ConfigFileReader() {}

  public static Map<String, String> read(InputStream input, String fileName) throws IOException {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".yaml") || lower.endsWith(".yml")) {
      return readYaml(input);
    }
    return readProperties(input);
  }

  public static Map<String, String> readProperties(InputStream input) throws IOException {
    Properties props = new Properties();
    props.load(input);
    Map<String, String> result = new LinkedHashMap<>();
    for (String key : props.stringPropertyNames()) {
      result.put(key, props.getProperty(key));
    }
    return result;
  }

  public static Map<String, String> readYaml(InputStream input) throws IOException {
    List<String> lines;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
      lines =
          reader.lines().filter(l -> !l.isBlank() && !l.stripLeading().startsWith("#")).toList();
    }

    Map<String, String> result = new LinkedHashMap<>();
    // Stack entries: [indentLevel, keySegment]
    Deque<int[]> indentStack = new ArrayDeque<>(); // stores indent levels
    Deque<String> keyStack = new ArrayDeque<>(); // stores key segments

    for (String line : lines) {
      int indent = countLeadingSpaces(line);
      String trimmed = line.strip();

      if (!trimmed.contains(":")) continue;

      int colonIdx = trimmed.indexOf(':');
      String key = trimmed.substring(0, colonIdx).strip();
      String value = trimmed.substring(colonIdx + 1).strip();

      // Remove inline comments from value
      value = stripInlineComment(value);

      // Strip surrounding quotes if present
      value = unquote(value);

      // Pop stack entries that are at the same or deeper indent level
      while (!indentStack.isEmpty() && indentStack.peek()[0] >= indent) {
        indentStack.pop();
        keyStack.pop();
      }

      if (value.isEmpty()) {
        // Parent key — push onto stack
        indentStack.push(new int[] {indent});
        keyStack.push(key);
      } else {
        // Leaf key — build full dotted path and store
        String fullKey = buildKey(keyStack, key);
        result.put(fullKey, value);
      }
    }

    return result;
  }

  // -------------------------------------------------------------------------

  private static int countLeadingSpaces(String line) {
    int count = 0;
    for (char c : line.toCharArray()) {
      if (c == ' ') count++;
      else break;
    }
    return count;
  }

  private static String buildKey(Deque<String> stack, String leaf) {
    if (stack.isEmpty()) return leaf;
    // Stack is LIFO so we need to reverse it to get parent → child order
    String[] parts = stack.toArray(new String[0]);
    StringBuilder sb = new StringBuilder();
    for (int i = parts.length - 1; i >= 0; i--) {
      sb.append(parts[i]).append('.');
    }
    sb.append(leaf);
    return sb.toString();
  }

  private static String stripInlineComment(String value) {
    // Only strip if not inside quotes
    if (value.startsWith("\"") || value.startsWith("'")) return value;
    int hashIdx = value.indexOf(" #");
    return hashIdx >= 0 ? value.substring(0, hashIdx).strip() : value;
  }

  private static String unquote(String value) {
    if ((value.startsWith("\"") && value.endsWith("\""))
        || (value.startsWith("'") && value.endsWith("'"))) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}
