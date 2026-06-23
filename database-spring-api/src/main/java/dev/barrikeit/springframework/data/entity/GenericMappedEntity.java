package dev.barrikeit.springframework.data.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@MappedSuperclass
public abstract class GenericMappedEntity<I extends Serializable, O extends BaseEntity>
    extends BaseEntity implements Persistable<I> {

  @Id protected I id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  protected O owner;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericMappedEntity<?, ?> that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "GenericMapped{" + "id=" + id + '}';
  }

  @Override
  public boolean isNew() {
    return getId() == null;
  }
}
