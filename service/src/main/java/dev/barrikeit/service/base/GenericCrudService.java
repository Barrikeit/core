package dev.barrikeit.service.base;

import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.GenericEntity;
import dev.barrikeit.springframework.data.repository.GenericRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>Generic Service Class</b>
 *
 * <p>abstract class that crud
 *
 * @param <E> the entity type that extends {@link GenericEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCrudService<
        E extends GenericEntity<I>, I extends Serializable, D extends BaseDto>
    extends GenericService<E, I, D> implements CrudableService<E, I, D> {

  private final GenericRepository<E, I> repository;
  private final BaseMapper<E, D> mapper;

  protected GenericCrudService(GenericRepository<E, I> repository, BaseMapper<E, D> mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public D save(D dto) {
    E entity = mapper.toEntity(dto);
    entity = repository.save(entity);
    return mapper.toDto(entity);
  }

  @Override
  @Transactional
  public E save(E entity) {
    return repository.save(entity);
  }

  @Override
  @Transactional
  public D update(I id, D dto) {
    E entity = findEntity(id);
    mapper.updateEntity(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  @Override
  @Transactional
  public void delete(I id) {
    E entity = findEntity(id);
    repository.delete(entity);
  }
}
