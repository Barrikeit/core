package dev.barrikeit.security.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Cryptographic hashing utilities. Pure JDK — no external dependencies. */
public final class HashEncodeUtil {

  private HashEncodeUtil() {
    throw new IllegalStateException("HashEncodeUtil class");
  }

  /**
   * Generates a Base64-encoded SHA-256 hash of the provided secret. Useful for deriving a
   * deterministic signing key from a passphrase stored in config files.
   *
   * @param secret the passphrase or secret string
   * @return standard Base64-encoded SHA-256 hash (not URL-safe)
   */
  public static String generateBase64Secret(String secret) {
    return Base64.getEncoder().encodeToString(sha256Bytes(secret));
  }

  /**
   * Returns the raw SHA-256 hash bytes of the provided string. Used internally by {@link JwtUtil}
   * to derive the HMAC signing key.
   *
   * @param secret the string to hash
   * @return 32-byte SHA-256 digest
   * @throws SecurityException if SHA-256 is unavailable (should never happen on any JVM)
   */
  public static byte[] sha256Bytes(String secret) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new SecurityException("SHA-256 algorithm not available", e);
    }
  }
}
