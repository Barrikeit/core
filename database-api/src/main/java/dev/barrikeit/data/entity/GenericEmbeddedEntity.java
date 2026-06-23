package dev.barrikeit.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class GenericEmbeddedEntity<I extends Serializable> extends BaseEntity
    implements Persistable<I> {

  @Serial private static final long serialVersionUID = 1L;

  protected I id;

  protected GenericEmbeddedEntity() {}

  public I getId() {
    return id;
  }

  public void setId(I id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericEmbeddedEntity<?> that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{id=" + id + "}";
  }

  @Override
  public boolean isNew() {
    return getId() == null;
  }
}
