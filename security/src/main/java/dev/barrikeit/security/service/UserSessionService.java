package dev.barrikeit.security.service;

import dev.barrikeit.exception.ValidationException;
import dev.barrikeit.security.data.entity.UserSession;
import dev.barrikeit.security.data.repository.UserSessionRepository;
import dev.barrikeit.security.util.TokenType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the lifecycle of JWT sessions persisted in {@link UserSession}.
 *
 * <p>Responsibilities: - Create a session record when a token pair is issued - Validate that a
 * token's jti still exists (not revoked) - Revoke token pairs on logout or refresh - Count active
 * sessions for concurrency control - Expose expired session cleanup for scheduled tasks
 *
 * <p>Shared by both REST and WS security configurations.
 */
@Service
@AllArgsConstructor
public class UserSessionService {

  private final UserSessionRepository repository;

  /**
   * Persists a new token session record.
   *
   * @param userId the user this token belongs to
   * @param jti this token's unique JWT ID
   * @param jtiPair the paired token's JWT ID (ACCESS ↔ REFRESH)
   * @param issuedAt when the token was issued
   * @param expiresAt when the token expires
   * @param tokenType "ACCESS" or "REFRESH"
   */
  public void createSession(
      UUID userId,
      String jti,
      String jtiPair,
      OffsetDateTime issuedAt,
      OffsetDateTime expiresAt,
      String tokenType) {
    repository.save(
        UserSession.builder()
            .userId(userId)
            .jti(jti)
            .jtiPair(jtiPair)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .tokenType(tokenType)
            .build());
  }

  public List<UserSession> findUserSessions(UUID userId) {
    return repository.findAllByUserId(userId);
  }

  /**
   * Finds a specific session by userId and jti.
   *
   * @throws ValidationException if the session does not exist
   */
  public UserSession findSession(UUID userId, String jti) {
    return repository
        .findByUserIdAndJti(userId, jti)
        .orElseThrow(
            () -> new ValidationException("Session not found for userId=%s jti=%s", userId, jti));
  }

  /**
   * Returns true if a session record exists for the given userId and jti. A missing record means
   * the token has been revoked.
   */
  public boolean validateToken(UUID userId, String jti) {
    return repository.existsByUserIdAndJti(userId, jti);
  }

  /**
   * Returns the number of active sessions of the given type for a user. Used to enforce {@code
   * security.max-concurrent-sessions}.
   */
  public int activeSessions(UUID userId, TokenType tokenType) {
    return repository.countByUserIdAndTokenType(userId, tokenType.name());
  }

  /**
   * Revokes an ACCESS/REFRESH token pair. Deletes both this token and its paired token from the
   * session store.
   *
   * @throws ValidationException if the session does not exist
   */
  @Transactional
  public void revokeTokenPair(UUID userId, String jti) {
    UserSession session =
        repository
            .findByUserIdAndJti(userId, jti)
            .orElseThrow(
                () ->
                    new ValidationException("Session not found for userId=%s jti=%s", userId, jti));

    repository.deleteByUserIdAndJti(userId, session.getJti());
    repository.deleteByUserIdAndJti(userId, session.getJtiPair());
  }

  /** Revokes all sessions for a user — used on account deletion or forced logout. */
  @Transactional
  public void revokeAll(UUID userId) {
    repository.deleteByUserId(userId);
  }

  /**
   * Deletes expired sessions. Call from a scheduled task:
   *
   * <pre>
   *   {@literal @}Scheduled(cron = "0 0 * * * *")
   *   public void cleanExpiredSessions() {
   *       userSessionService.deleteExpiredSessions();
   *   }
   * </pre>
   */
  @Transactional
  public void deleteExpiredSessions() {
    repository.deleteByExpiresAtBefore(OffsetDateTime.now());
  }
}
