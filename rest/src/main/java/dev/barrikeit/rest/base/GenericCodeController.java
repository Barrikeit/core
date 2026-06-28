package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.GenericCodeEntity;
import dev.barrikeit.service.base.GenericCodeService;
import dev.barrikeit.data.dto.BaseDto;
import java.io.Serializable;
import lombok.extern.log4j.Log4j2;

/**
 * <b>Generic Code Controller Class</b>
 *
 * <p>
 *
 * @param <E> the entity type that extends {@link GenericCodeEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <C> the type of the entity's code, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 */
@Log4j2
public abstract class GenericCodeController<
        E extends GenericCodeEntity<I, C>,
        I extends Serializable,
        C extends Serializable,
        D extends BaseDto>
    extends GenericController<E, I, D> {

  private final GenericCodeService<E, I, C, D> service;

  protected GenericCodeController(GenericCodeService<E, I, C, D> service) {
    super(service);
    this.service = service;
  }

  /**
   * Retrieves a specific DTO by its code.
   *
   * @param code the code of the entity to retrieve.
   * @return a response entity containing the requested DTO.
   */
  protected Response<D> findByCode(C code) {
    return Response.ok(service.findByCode(code));
  }
}
