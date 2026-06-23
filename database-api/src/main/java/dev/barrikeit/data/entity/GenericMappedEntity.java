package dev.barrikeit.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class GenericMappedEntity<I extends Serializable, O extends BaseEntity>
    extends BaseEntity implements Persistable<I> {

  @Serial private static final long serialVersionUID = 1L;

  protected I id;
  protected O owner;

  protected GenericMappedEntity() {}

  public I getId() {
    return id;
  }

  public void setId(I id) {
    this.id = id;
  }

  public O getOwner() {
    return owner;
  }

  public void setOwner(O owner) {
    this.owner = owner;
  }

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
    return getClass().getSimpleName() + "{id=" + id + "}";
  }

  public boolean isNew() {
    return getId() == null;
  }
}
