package dev.barrikeit.data.entity;

import java.io.Serial;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  public void onPrePersist() {}

  public void onPreUpdate() {}
}
