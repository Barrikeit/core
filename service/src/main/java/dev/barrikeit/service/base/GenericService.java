package dev.barrikeit.service.base;

import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.GenericEntity;
import dev.barrikeit.springframework.data.repository.GenericRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;

/**
 * <b>Generic Service Class</b>
 *
 * <p>This abstract class provides a generic implementation of service operations for managing
 * entities and their corresponding DTOs. It interacts with a {@link GenericRepository} for data
 * access and uses a {@link BaseMapper} for object mapping between entities and DTOs.
 *
 * @param <E> the entity type that extends {@link GenericEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericService<
        E extends GenericEntity<I>, I extends Serializable, D extends BaseDto>
    extends BaseService<E, I, D> {

  protected GenericService(GenericRepository<E, I> repository, BaseMapper<E, D> mapper) {
    super(repository, mapper);
  }
}
