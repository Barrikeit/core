package dev.barrikeit.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class GenericCodeEntity<I extends Serializable, C extends Serializable>
    extends GenericEntity<I> {

  @Serial private static final long serialVersionUID = 1L;

  protected C code;

  protected GenericCodeEntity() {}

  public C getCode() {
    return code;
  }

  public void setCode(C code) {
    this.code = code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericCodeEntity<?, ?> that)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (code != null ? code.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{id=" + id + ", code=" + code + "}";
  }
}
