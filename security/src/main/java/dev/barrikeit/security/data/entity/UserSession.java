package dev.barrikeit.security.data.entity;

import dev.barrikeit.springframework.data.entity.GenericEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Tracks active JWT sessions for revocation and concurrency control.
 *
 * <p>Each row represents one issued token (ACCESS or REFRESH). The {@code jtiPair} field links an
 * ACCESS token to its paired REFRESH token and vice versa — enabling pair revocation on logout or
 * refresh.
 *
 * <p>Table: user_sessions
 */
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user_sessions")
public class UserSession extends GenericEntity<UUID> {

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  /** JWT ID (jti claim) — unique identifier for this token. */
  @Column(name = "jti", nullable = false, unique = true)
  private String jti;

  /**
   * JWT ID of the paired token. For an ACCESS token, this is the paired REFRESH token's jti. For a
   * REFRESH token, this is the paired ACCESS token's jti.
   */
  @Column(name = "jti_pair", nullable = false)
  private String jtiPair;

  @Column(name = "issued_at", nullable = false)
  private OffsetDateTime issuedAt;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  /** Token type — "ACCESS" or "REFRESH". See {@link dev.barrikeit.security.util.TokenType}. */
  @Column(name = "token_type", nullable = false)
  private String tokenType;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UserSession that)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(id, that.id)
        && Objects.equals(jti, that.jti)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
