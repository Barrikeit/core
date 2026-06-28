package dev.barrikeit.rest.base;

import dev.barrikeit.springframework.data.entity.GenericEntity;
import dev.barrikeit.service.base.GenericService;
import dev.barrikeit.data.dto.BaseDto;
import java.io.Serializable;
import lombok.extern.log4j.Log4j2;

/**
 * <b>Generic Controller Class</b>
 *
 * <p>
 *
 * @param <E> the entity type that extends {@link GenericEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericController<
        E extends GenericEntity<I>, I extends Serializable, D extends BaseDto>
    extends BaseController<E, I, D> {

  protected GenericController(GenericService<E, I, D> service) {
    super(service);
  }
}
