package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.service.base.CrudBaseService;
import dev.barrikeit.data.dto.BaseDto;
import java.io.Serializable;
import lombok.extern.log4j.Log4j2;

/**
 * <b>CRUD Base Controller Class</b>
 *
 * <p>This abstract class provides common RESTful endpoint implementations for managing entities.
 *
 * @param <E> the entity type that extends {@link BaseEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class CrudBaseController<
        E extends BaseEntity, I extends Serializable, D extends BaseDto>
    extends BaseController<E, I, D> {

  private final CrudBaseService<E, I, D> crudService;

  protected CrudBaseController(CrudBaseService<E, I, D> crudService) {
    super(crudService);
    this.crudService = crudService;
  }

  /**
   * Saves a new entity represented by the provided DTO.
   *
   * @param dto the DTO representing the entity to save.
   * @return a response entity containing the saved DTO.
   */
  public Response<D> save(D dto) {
    return Response.ok(crudService.save(dto));
  }

  /**
   * Updates an existing entity identified by its identifier with the provided DTO.
   *
   * @param id the identifier of the entity to update.
   * @param dto the DTO containing the updated entity information.
   * @return a response entity containing the updated DTO.
   */
  public Response<D> update(I id, D dto) {
    return Response.ok(crudService.update(id, dto));
  }

  /**
   * Deletes an entity identified by its identifier.
   *
   * @param id the identifier of the entity to delete.
   * @return a response entity indicating the operation's result.
   */
  public Response<Void> delete(I id) {
    crudService.delete(id);
    return Response.noContent("Eliminado con éxito");
  }
}
