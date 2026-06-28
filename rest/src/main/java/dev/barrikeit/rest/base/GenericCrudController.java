package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.GenericEntity;
import dev.barrikeit.service.base.GenericCrudService;
import dev.barrikeit.data.dto.BaseDto;
import jakarta.validation.Valid;
import java.io.Serializable;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <b>Generic CRUD Controller Class</b>
 *
 * <p>
 *
 * @param <E> the entity type that extends {@link GenericEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCrudController<
        E extends GenericEntity<I>, I extends Serializable, D extends BaseDto>
    extends GenericController<E, I, D> implements GenericCrudApi<I, D> {

  private final GenericCrudService<E, I, D> crudService;

  protected GenericCrudController(GenericCrudService<E, I, D> crudService) {
    super(crudService);
    this.crudService = crudService;
  }

  /**
   * Saves a new entity represented by the provided DTO.
   *
   * @param dto the DTO representing the entity to save.
   * @return a response entity containing the saved DTO.
   */
  @PostMapping()
  public Response<D> save(@Valid @RequestBody D dto) {
    return Response.ok(crudService.save(dto));
  }

  /**
   * Updates an existing entity identified by its identifier with the provided DTO.
   *
   * @param id the identifier of the entity to update.
   * @param dto the DTO containing the updated entity information.
   * @return a response entity containing the updated DTO.
   */
  @PutMapping("/id/{id}/update")
  public Response<D> update(@PathVariable("id") I id, @RequestBody D dto) {
    return Response.ok(crudService.update(id, dto));
  }

  /**
   * Deletes an entity identified by its identifier.
   *
   * @param id the identifier of the entity to delete.
   * @return a response entity indicating the operation's result.
   */
  @DeleteMapping("/id/{id}")
  public Response<Void> delete(@PathVariable("id") I id) {
    crudService.delete(id);
    return Response.noContent("Eliminado con éxito");
  }
}
