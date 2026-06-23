package dev.barrikeit.security.util;

/**
 * JWT claim name constants. Used by {@link JwtUtil} when building and parsing tokens, and by
 * filters/interceptors when reading claims.
 */
public final class JwtConstants {

  private JwtConstants() {
    throw new IllegalStateException("Constants class");
  }

  public static final String JWT = "access-token";
  public static final String JWT_REFRESH = "refresh-token";

  // Custom claim keys
  public static final String USER = "user";
  public static final String REFRESHABLE = "refreshable";
  public static final String ROLES = "roles";
  public static final String AUTHORITIES = "authorities";
}
