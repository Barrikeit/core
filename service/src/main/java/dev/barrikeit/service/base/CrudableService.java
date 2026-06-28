package dev.barrikeit.service.base;

import java.io.Serializable;
import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.data.dto.BaseDto;

public interface CrudableService<E extends BaseEntity, I extends Serializable, D extends BaseDto> {
  D save(D dto);

  E save(E entity);

  D update(I id, D dto);

  void delete(I id);
}
