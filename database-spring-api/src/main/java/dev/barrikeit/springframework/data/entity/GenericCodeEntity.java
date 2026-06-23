package dev.barrikeit.springframework.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@MappedSuperclass
public abstract class GenericCodeEntity<I extends Serializable, C extends Serializable>
    extends GenericEntity<I> {
  @Serial private static final long serialVersionUID = 1L;

  @NotNull
  @Column(name = "code", updatable = false, nullable = false, unique = true)
  protected C code;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GenericCodeEntity<? extends Serializable, ? extends Serializable> that))
      return false;
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
    return "GenericCode{" + "code=" + code + '}';
  }
}
