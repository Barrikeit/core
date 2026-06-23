package dev.barrikeit.springframework.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Extends {@link GenericEntity} with JPA auditing fields.
 *
 * <p>Requires {@code @EnableJpaAuditing} in the application configuration and an
 * {@code AuditorAware<String>} bean that returns the current principal's identifier.
 *
 * <p>Usage:
 *
 * <pre>
 * {@literal @}Entity
 * public class Product extends AuditedEntity<UUID> {
 *     // domain fields only — createdAt, createdBy, etc. are inherited
 * }
 * </pre>
 */
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity<I extends java.io.Serializable> extends GenericEntity<I> {

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by", updatable = false, length = 150)
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", length = 150)
  private String updatedBy;
}
