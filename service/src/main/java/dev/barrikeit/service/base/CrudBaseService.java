package dev.barrikeit.service.base;

import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.springframework.data.repository.BaseRepository;
import dev.barrikeit.springframework.data.repository.CrudBaseRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>CRUD Base Service Class</b>
 *
 * <p>This abstract class provides a basic implementation of service operations for managing
 * entities and their corresponding DTOs. It interacts with a {@link CrudBaseRepository} for data
 * access and uses a {@link BaseMapper} for object mapping between entities and DTOs.
 *
 * @param <E> the entity type that extends {@link BaseEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public class CrudBaseService<E extends BaseEntity, I extends Serializable, D extends BaseDto>
    extends BaseService<E, I, D> implements CrudableService<E, I, D> {

  private final BaseRepository<E, I> repository;
  private final BaseMapper<E, D> mapper;

  protected CrudBaseService(BaseRepository<E, I> repository, BaseMapper<E, D> mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Saves a new entity represented by the provided DTO.
   *
   * @param dto the DTO representing the entity to save.
   * @return the saved DTO.
   */
  @Transactional
  public D save(D dto) {
    E entity = mapper.toEntity(dto);
    entity = repository.save(entity);
    return mapper.toDto(entity);
  }

  /**
   * Saves a new entity directly.
   *
   * @param entity the entity to save.
   * @return the saved entity.
   */
  @Transactional
  public E save(E entity) {
    return repository.save(entity);
  }

  /**
   * Updates an existing entity identified by its identifier with the provided DTO.
   *
   * @param id the identifier of the entity to update.
   * @param dto the DTO containing the updated entity information.
   * @return the updated DTO.
   */
  @Transactional
  public D update(I id, D dto) {
    E entity = findEntity(id);
    mapper.updateEntity(dto, entity);
    return mapper.toDto(repository.save(entity));
  }

  /**
   * Deletes an entity identified by its identifier.
   *
   * @param id the identifier of the entity to delete.
   */
  @Transactional
  public void delete(I id) {
    repository.deleteById(id);
  }
}
