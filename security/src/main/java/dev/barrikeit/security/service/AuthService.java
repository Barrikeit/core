package dev.barrikeit.security.service;

import dev.barrikeit.exception.BadRequestException;
import dev.barrikeit.exception.UnauthorizedException;
import dev.barrikeit.security.config.SecurityProperties;
import dev.barrikeit.security.data.entity.BasicUserDetails;
import dev.barrikeit.security.data.entity.UserSession;
import dev.barrikeit.security.rest.dto.JwtDto;
import dev.barrikeit.security.rest.dto.LoginDto;
import dev.barrikeit.security.util.JwtUtil;
import dev.barrikeit.security.util.TokenType;
import dev.barrikeit.util.TimeUtil;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Core authentication service — login, logout, refresh, and optional registration.
 *
 * <p>This service is generic and reusable across applications. It delegates user loading to {@link
 * BasicUserDetailsService} and token operations to {@link JwtUtil}. Session persistence is handled
 * by {@link UserSessionService}.
 *
 * <p>Registration is intentionally left as a no-op here — child projects that need registration
 * should override {@link #register(dev.barrikeit.security.rest.dto.RegisterDto)} in a subclass or
 * provide a separate registration flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final SecurityProperties securityProperties;
  private final BasicUserDetailsService userDetailsService;
  private final UserSessionService userSessionService;
  private final JwtUtil jwtUtil;

  // -------------------------------------------------------------------------
  // Login
  // -------------------------------------------------------------------------

  public JwtDto login(LoginDto dto) {
    BasicUserDetails user = userDetailsService.authenticate(dto.getUsername(), dto.getPassword());
    return issueTokenPair(user);
  }

  // -------------------------------------------------------------------------
  // Refresh
  // -------------------------------------------------------------------------

  public JwtDto refresh(String accessToken, String refreshToken) {
    try {
      if (!jwtUtil.isRefreshableToken(refreshToken)) {
        throw new UnauthorizedException("exception.auth.token-invalid");
      }
    } catch (ExpiredJwtException e) {
      throw new UnauthorizedException("exception.auth.token-expired");
    }

    UUID refreshUserId = jwtUtil.extractUserId(refreshToken);
    String refreshJti = jwtUtil.extractJti(refreshToken);
    UserSession refreshSession = userSessionService.findSession(refreshUserId, refreshJti);

    UUID accessUserId = jwtUtil.extractUserId(accessToken);
    String accessJti = jwtUtil.extractJti(accessToken);

    if (!refreshSession.getUserId().equals(accessUserId)) {
      throw new UnauthorizedException("exception.auth.token-invalid");
    }
    if (!refreshSession.getJtiPair().equals(accessJti)) {
      throw new UnauthorizedException("exception.auth.token-invalid");
    }

    BasicUserDetails user = userDetailsService.loadUserByCode(accessUserId);
    userSessionService.revokeTokenPair(accessUserId, accessJti);
    return issueTokenPair(user);
  }

  // -------------------------------------------------------------------------
  // Logout
  // -------------------------------------------------------------------------

  public void logout(String accessToken) {
    UUID userId = jwtUtil.extractUserId(accessToken);
    String jti = jwtUtil.extractJti(accessToken);
    userSessionService.revokeTokenPair(userId, jti);
  }

  // -------------------------------------------------------------------------
  // Registration — override in child service
  // -------------------------------------------------------------------------

  public String register(dev.barrikeit.security.rest.dto.RegisterDto dto) {
    throw new UnsupportedOperationException(
        "Registration is not implemented. Override AuthService.register() in your application.");
  }

  // -------------------------------------------------------------------------
  // Token pair issuance
  // -------------------------------------------------------------------------

  private JwtDto issueTokenPair(BasicUserDetails user) {
    UUID userId = user.getId();

    int activeSessions = userSessionService.activeSessions(userId, TokenType.ACCESS);
    Integer maxSessions = securityProperties.getMaxConcurrentSessions();
    if (maxSessions != null && activeSessions >= maxSessions) {
      throw new BadRequestException("exception.auth.max-sessions", user.getUsername());
    }

    String accessJti = UUID.randomUUID().toString();
    String refreshJti = UUID.randomUUID().toString();

    String accessToken = jwtUtil.generateAccessToken(user, accessJti);
    String refreshToken = jwtUtil.generateRefreshToken(user, refreshJti);

    var issuedAt = TimeUtil.offsetDateTimeNow();
    var accessExpiry = TimeUtil.toOffsetDateTime(jwtUtil.extractExpirationDate(accessToken));
    var refreshExpiry = TimeUtil.toOffsetDateTime(jwtUtil.extractExpirationDate(refreshToken));

    userSessionService.createSession(
        userId, accessJti, refreshJti, issuedAt, accessExpiry, TokenType.ACCESS.name());
    userSessionService.createSession(
        userId, refreshJti, accessJti, issuedAt, refreshExpiry, TokenType.REFRESH.name());

    return JwtDto.builder()
        .token(accessToken)
        .refreshToken(refreshToken)
        .expireAt(jwtUtil.extractExpirationDate(accessToken))
        .expireRefreshAt(jwtUtil.extractExpirationDate(refreshToken))
        .build();
  }
}
