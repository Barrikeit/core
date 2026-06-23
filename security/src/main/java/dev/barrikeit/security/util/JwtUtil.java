package dev.barrikeit.security.util;

import dev.barrikeit.security.config.SecurityProperties;
import dev.barrikeit.security.data.entity.BasicUserDetails;
import dev.barrikeit.util.TimeUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * JWT utility — generates and parses signed JWTs.
 *
 * <p>Uses HMAC-SHA256 (HS256) with a key derived from the configured secret via SHA-256 hashing
 * ({@link HashEncodeUtil#sha256Bytes(String)}).
 *
 * <p>Registered as a Spring {@code @Component} so it can be injected into filters, interceptors,
 * and services in child projects.
 *
 * <p>Token structure: Standard claims: jti, sub (username), iss (issuer), iat, exp Custom claims:
 * user (UUID), roles (List<String>), authorities (List<String>), refreshable (boolean)
 */
@Service
public class JwtUtil {

  private final SecretKey secretKey;
  private final String issuer;
  private final long accessTokenExpirationSec;
  private final long refreshTokenExpirationSec;

  public JwtUtil(SecurityProperties properties) {
    SecurityProperties.JwtProperties jwt = properties.getJwt();
    this.secretKey = Keys.hmacShaKeyFor(HashEncodeUtil.sha256Bytes(jwt.getSecret()));
    this.issuer = jwt.getIssuer();
    this.accessTokenExpirationSec = jwt.getExpiration();
    this.refreshTokenExpirationSec = jwt.getExpirationRefresh();
  }

  // -------------------------------------------------------------------------
  // Token generation
  // -------------------------------------------------------------------------

  /**
   * Generates a short-lived ACCESS token for the given user.
   *
   * @param userDetails the authenticated user
   * @param jti unique token identifier (UUID string) — stored in session for revocation
   */
  public String generateAccessToken(BasicUserDetails userDetails, String jti) {
    return buildToken(userDetails, jti, accessTokenExpirationSec, false);
  }

  /**
   * Generates a long-lived REFRESH token for the given user.
   *
   * @param userDetails the authenticated user
   * @param jti unique token identifier (UUID string)
   */
  public String generateRefreshToken(BasicUserDetails userDetails, String jti) {
    return buildToken(userDetails, jti, refreshTokenExpirationSec, true);
  }

  private String buildToken(
      BasicUserDetails user, String jti, long expirationSeconds, boolean refreshable) {
    Instant now = TimeUtil.instantNow();
    return Jwts.builder()
        .id(jti)
        .subject(user.getUsername())
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expirationSeconds)))
        .claim(JwtConstants.USER, user.getId().toString())
        .claim(JwtConstants.ROLES, user.getRolesNames())
        .claim(JwtConstants.AUTHORITIES, user.getAuthorityNames())
        .claim(JwtConstants.REFRESHABLE, refreshable)
        .signWith(secretKey)
        .compact();
  }

  // -------------------------------------------------------------------------
  // Token parsing
  // -------------------------------------------------------------------------

  /**
   * Parses and validates a JWT, returning its claims. Throws JJWT exceptions on invalid or expired
   * tokens — callers handle them.
   */
  public Claims parseToken(String token) {
    return Jwts.parser()
        .requireIssuer(issuer)
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  // -------------------------------------------------------------------------
  // Claim extractors
  // -------------------------------------------------------------------------

  public String extractJti(String token) {
    return parseToken(token).getId();
  }

  public String extractUsername(String token) {
    return parseToken(token).getSubject();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(parseToken(token).get(JwtConstants.USER, String.class));
  }

  public Date extractIssuedDate(String token) {
    return parseToken(token).getIssuedAt();
  }

  public Date extractExpirationDate(String token) {
    return parseToken(token).getExpiration();
  }

  public boolean isRefreshableToken(String token) {
    return Boolean.TRUE.equals(parseToken(token).get(JwtConstants.REFRESHABLE, Boolean.class));
  }

  public List<String> extractRoles(String token) {
    List<String> roles = parseToken(token).get(JwtConstants.ROLES, List.class);
    return roles != null ? roles : Collections.emptyList();
  }

  public List<SimpleGrantedAuthority> extractAuthorities(String token) {
    return extractRoles(token).stream().map(SimpleGrantedAuthority::new).toList();
  }
}
