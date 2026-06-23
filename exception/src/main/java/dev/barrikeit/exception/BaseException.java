package dev.barrikeit.exception;

import java.io.Serial;
import java.text.MessageFormat;

/**
 * Base runtime exception for the application.
 *
 * <p>Pure data carrier — holds a message and optional arguments. Performs no side effects (no
 * logging, no resolution).
 *
 * <p>The message can be:
 *
 * <ul>
 *   <li>An i18n key (by convention starting with {@code "exception."}) to be resolved later by the
 *       exception handler via {@code MessageSource}.
 *   <li>A plain string with {@link MessageFormat} placeholders: {@code {0}, {1}}
 *   <li>A plain string with {@link String#format} placeholders: {@code %s, %d}
 *   <li>A plain string with no placeholders.
 * </ul>
 *
 * <p>Both placeholder styles are supported. {@code {0}, {1}} is tried first (consistent with
 * Spring's {@code MessageSource}), falling back to {@code %s, %d} if no substitution occurred.
 */
public class BaseException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final Object[] messageArgs;

  public BaseException(String message) {
    super(message);
    this.messageArgs = null;
  }

  public BaseException(String message, Object... messageArgs) {
    super(message);
    this.messageArgs = messageArgs;
  }

  public BaseException(String message, Throwable cause) {
    super(message, cause);
    this.messageArgs = null;
  }

  public BaseException(String message, Throwable cause, Object... messageArgs) {
    super(message, cause);
    this.messageArgs = messageArgs;
  }

  public Object[] getMessageArgs() {
    return messageArgs;
  }

  /**
   * Returns a display-ready message for contexts where no {@code MessageSource} is available (e.g.
   * log lines).
   *
   * <p>i18n keys are returned as-is (resolved later by the handler). Plain strings are formatted by
   * trying {@code MessageFormat} first, then {@code String.format} as fallback.
   */
  public String getFormattedMessage() {
    String raw = getMessage();
    if (raw == null) return null;
    if (isI18nKey() || messageArgs == null || messageArgs.length == 0) {
      return raw;
    }
    return formatMessage(raw, messageArgs);
  }

  /** Convention: keys starting with "exception." are resolved via MessageSource. */
  public boolean isI18nKey() {
    return getMessage() != null && getMessage().startsWith("exception.");
  }

  /**
   * Tries {@code MessageFormat} ({0}, {1}) first. If the result is identical to the input (meaning
   * no placeholders were substituted), falls back to {@code String.format} (%s, %d). If both fail,
   * returns the raw string.
   */
  static String formatMessage(String pattern, Object[] args) {
    try {
      String result = MessageFormat.format(pattern, args);
      if (!result.equals(pattern)) {
        return result;
      }
    } catch (Exception ignored) {
    }

    try {
      return String.format(pattern, args);
    } catch (Exception ignored) {
    }

    return pattern;
  }
}
