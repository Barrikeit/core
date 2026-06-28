package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.GenericCodeEntity;
import dev.barrikeit.service.base.GenericCodeCrudService;
import dev.barrikeit.data.dto.BaseDto;
import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <b>Generic Code CRUD Controller Class</b>
 *
 * <p>
 *
 * @param <E> the entity type that extends {@link GenericCodeEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <C> the type of the entity's code, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCodeCrudController<
        E extends GenericCodeEntity<I, C>,
        I extends Serializable,
        C extends Serializable,
        D extends BaseDto>
    extends GenericCodeController<E, I, C, D> implements GenericCodeCrudApi<C, D> {

  private final GenericCodeCrudService<E, I, C, D> crudService;

  protected GenericCodeCrudController(GenericCodeCrudService<E, I, C, D> crudService) {
    super(crudService);
    this.crudService = crudService;
  }

  /**
   * Updates an existing entity identified by its code with the provided DTO.
   *
   * @param code the code of the entity to update.
   * @param dto the DTO containing the updated entity information.
   * @return a response entity containing the updated DTO.
   */
  @PutMapping("/code/{code}/update")
  public Response<D> updateByCode(@PathVariable("code") C code, @RequestBody D dto) {
    return Response.ok(crudService.updateByCode(code, dto));
  }

  /**
   * Deletes an entity identified by its code.
   *
   * @param code the code of the entity to delete.
   * @return a response entity indicating the operation's result.
   */
  @DeleteMapping("/code/{code}")
  public Response<Void> deleteByCode(@PathVariable("code") C code) {
    crudService.deleteByCode(code);
    return Response.noContent("Eliminado con éxito");
  }
}
