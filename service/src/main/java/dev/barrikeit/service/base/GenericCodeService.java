package dev.barrikeit.service.base;

import java.io.Serializable;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.GenericCodeEntity;
import dev.barrikeit.springframework.data.repository.GenericCodeRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.mapper.BaseMapper;
import dev.barrikeit.filter.constants.EntityConstants;
import dev.barrikeit.filter.constants.ExceptionConstants;
import dev.barrikeit.exception.NotFoundException;
import org.springframework.data.domain.Sort;

/**
 * <b>Generic Code Service Class</b>
 *
 * <p>This abstract class provides a generic implementation of service operations for managing
 * entities and their corresponding DTOs. It interacts with a {@link GenericCodeRepository} for data
 * access and uses a {@link BaseMapper} for object mapping between entities and DTOs.
 *
 * @param <E> the entity type that extends {@link GenericCodeEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <C> the type of the entity's code, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCodeService<
        E extends GenericCodeEntity<I, C>,
        I extends Serializable,
        C extends Serializable,
        D extends BaseDto>
    extends GenericService<E, I, D> {

  private final GenericCodeRepository<E, I, C> repository;
  private final BaseMapper<E, D> mapper;

  protected GenericCodeService(GenericCodeRepository<E, I, C> repository, BaseMapper<E, D> mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Retrieves a list of all DTOs sorted by their code.
   *
   * @return a list of DTOs representing all entities.
   */
  @Override
  public List<D> findAll() {
    return repository.findAll(Sort.by(Sort.Direction.ASC, EntityConstants.CODE)).stream()
        .map(this.mapper::toDto)
        .toList();
  }

  /**
   * Retrieves a list of all entities sorted by their code.
   *
   * @return a list of entities.
   */
  @Override
  public List<E> findAllEntity() {
    return repository.findAll(Sort.by(Sort.Direction.ASC, EntityConstants.CODE)).stream().toList();
  }

  /**
   * Retrieves a DTO by its identifier.
   *
   * @param code the code of the entity to retrieve.
   * @return the DTO corresponding to the entity.
   * @throws NotFoundException if the entity is not found.
   */
  public D findByCode(C code) {
    return repository
        .findByCode(code)
        .map(this.mapper::toDto)
        .orElseThrow(() -> new NotFoundException(ExceptionConstants.NOT_FOUND, code));
  }

  /**
   * Retrieves an entity by its identifier.
   *
   * @param code the code of the entity to retrieve.
   * @return the entity.
   * @throws NotFoundException if the entity is not found.
   */
  public E findEntityByCode(C code) {
    return repository
        .findByCode(code)
        .orElseThrow(() -> new NotFoundException(ExceptionConstants.NOT_FOUND, code));
  }
}
