package dev.barrikeit.data.entity;

import java.io.Serializable;

public interface Persistable<I extends Serializable> {
  I getId();

  boolean isNew();

  default void initId() {}
}
