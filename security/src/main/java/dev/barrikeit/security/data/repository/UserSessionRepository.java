package dev.barrikeit.security.data.repository;

import dev.barrikeit.security.data.entity.UserSession;
import dev.barrikeit.springframework.data.repository.GenericRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for {@link UserSession} — active JWT session tracking.
 *
 * <p>Extends {@link GenericRepository} which provides standard CRUD operations via {@code
 * JpaRepository<UserSession, UUID>}.
 */
@Repository
public interface UserSessionRepository extends GenericRepository<UserSession, UUID> {

  List<UserSession> findAllByUserId(UUID userId);

  Optional<UserSession> findByUserIdAndJti(UUID userId, String jti);

  boolean existsByUserIdAndJti(UUID userId, String jti);

  int countByUserIdAndTokenType(UUID userId, String tokenType);

  void deleteByUserId(UUID userId);

  void deleteByUserIdAndJti(UUID userId, String jti);

  /** Cleans up expired sessions — call periodically via a scheduler. */
  @Transactional
  void deleteByExpiresAtBefore(OffsetDateTime now);
}
