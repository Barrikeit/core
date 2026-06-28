package dev.barrikeit.service.base;

import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.GenericCodeEntity;
import dev.barrikeit.springframework.data.repository.GenericCodeRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>Generic Code Service Class</b>
 *
 * <p>abstract class that crud
 *
 * @param <E> the entity type that extends {@link GenericCodeEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <C> the type of the entity's code, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCodeCrudService<
        E extends GenericCodeEntity<I, C>,
        I extends Serializable,
        C extends Serializable,
        D extends BaseDto>
    extends GenericCodeService<E, I, C, D> implements CrudableService<E, I, D> {

  private final GenericCodeRepository<E, I, C> repository;
  private final BaseMapper<E, D> mapper;

  protected GenericCodeCrudService(
      GenericCodeRepository<E, I, C> repository, BaseMapper<E, D> mapper) {
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

  /**
   * Updates an existing entity identified by its code with the provided DTO.
   *
   * @param code the code of the entity to update.
   * @param dto the DTO containing the updated entity information.
   * @return the updated DTO.
   */
  @Transactional
  public D updateByCode(C code, D dto) {
    E entity = findEntityByCode(code);
    mapper.updateEntity(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  /**
   * Deletes an entity identified by its code.
   *
   * @param code the code of the entity to delete.
   */
  @Transactional
  public void deleteByCode(C code) {
    E entity = findEntityByCode(code);
    repository.delete(entity);
  }
}
