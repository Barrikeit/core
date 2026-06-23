package dev.barrikeit.security.util;

/**
 * JWT token types. ACCESS tokens are short-lived and used for API authentication. REFRESH tokens
 * are long-lived and used to obtain new ACCESS tokens.
 */
public enum TokenType {
  ACCESS,
  REFRESH
}
