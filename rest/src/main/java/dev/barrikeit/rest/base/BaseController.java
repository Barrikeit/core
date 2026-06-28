package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.service.base.BaseService;
import dev.barrikeit.data.dto.BaseDto;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * <b>Base Controller Class</b>
 *
 * <p>This abstract class provides common RESTful endpoint implementations for finding entities. It
 * relies on a {@link BaseService} to handle data access.
 *
 * @param <E> the entity type that extends {@link BaseEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
@AllArgsConstructor
public abstract class BaseController<
    E extends BaseEntity, I extends Serializable, D extends BaseDto> {

  private final BaseService<E, I, D> baseService;

  /**
   * Retrieves a list of all DTOs.
   *
   * @return a response entity containing a list of DTOs.
   */
  protected Response<List<D>> findAll() {
    return Response.ok(baseService.findAll());
  }

  /**
   * Retrieves a specific DTO by its identifier.
   *
   * @param id the identifier of the entity to retrieve.
   * @return a response entity containing the requested DTO.
   */
  protected Response<D> findById(I id) {
    return Response.ok(baseService.find(id));
  }
}
