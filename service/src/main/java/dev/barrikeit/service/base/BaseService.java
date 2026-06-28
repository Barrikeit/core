package dev.barrikeit.service.base;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.springframework.data.repository.BaseRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;
import dev.barrikeit.filter.constants.EntityConstants;
import dev.barrikeit.filter.constants.ExceptionConstants;
import dev.barrikeit.exception.NotFoundException;
import org.springframework.data.domain.Sort;

/**
 * <b>Base Service Class</b>
 *
 * <p>This abstract class provides a basic implementation of service operations for finding
 * entities and their corresponding DTOs. It interacts with a {@link BaseRepository} for data
 * access and uses a {@link BaseMapper} for object mapping between entities and DTOs.
 *
 * @param <E> the entity type that extends {@link BaseEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
@AllArgsConstructor
public abstract class BaseService<E extends BaseEntity, I extends Serializable, D extends BaseDto> {

  private final BaseRepository<E, I> repository;
  private final BaseMapper<E, D> mapper;

  /**
   * Retrieves a list of all DTOs sorted by their identifier.
   *
   * @return a list of DTOs representing all entities.
   */
  public List<D> findAll() {
    return repository.findAll(Sort.by(Sort.Direction.ASC, EntityConstants.ID)).stream()
        .map(mapper::toDto)
        .toList();
  }

  /**
   * Retrieves a list of all DTOs sorted by the specified sort criteria.
   *
   * @param sort the sorting criteria.
   * @return a list of DTOs representing all entities.
   */
  public List<D> findAll(Sort sort) {
    return repository.findAll(sort).stream().map(mapper::toDto).toList();
  }

  /**
   * Retrieves a list of all entities sorted by their identifier.
   *
   * @return a list of entities.
   */
  public List<E> findAllEntity() {
    return repository.findAll(Sort.by(Sort.Direction.ASC, EntityConstants.ID)).stream().toList();
  }

  /**
   * Retrieves a list of all entities sorted by the specified sort criteria.
   *
   * @param sort the sorting criteria.
   * @return a list of entities.
   */
  public List<E> findAllEntity(Sort sort) {
    return repository.findAll(sort).stream().toList();
  }

  /**
   * Retrieves a DTO by its identifier.
   *
   * @param id the identifier of the entity to retrieve.
   * @return the DTO corresponding to the entity.
   * @throws NotFoundException if the entity is not found.
   */
  public D find(I id) {
    return repository
        .findById(id)
        .map(mapper::toDto)
        .orElseThrow(() -> new NotFoundException(ExceptionConstants.NOT_FOUND, id));
  }

  /**
   * Retrieves an entity by its identifier.
   *
   * @param id the identifier of the entity to retrieve.
   * @return the entity.
   * @throws NotFoundException if the entity is not found.
   */
  public E findEntity(I id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ExceptionConstants.NOT_FOUND, id));
  }
}
