package dev.barrikeit.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Uses {@link SecureRandom} instead of {@link java.util.Random} to ensure cryptographically strong
 * random values suitable for tokens, salts, and identifiers.
 */
public final class RandomUtil {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private RandomUtil() {
    throw new IllegalStateException("RandomUtil class");
  }

  public static String randomBase64(int length) {
    byte[] bytes = new byte[length];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static UUID randomUUID() {
    return UUID.randomUUID();
  }

  public static int randomInt(int min, int max) {
    return SECURE_RANDOM.nextInt(max - min) + min;
  }

  public static String randomNumericCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(SECURE_RANDOM.nextInt(10));
    }
    return sb.toString();
  }
}
