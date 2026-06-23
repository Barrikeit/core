package dev.barrikeit.springframework.data.entity;

import jakarta.persistence.MappedSuperclass;
import java.io.Serial;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity extends dev.barrikeit.data.entity.BaseEntity {
  @Serial private static final long serialVersionUID = 1L;
}
