package dev.barrikeit.rest.base;

import dev.barrikeit.rest.Response;

import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.service.base.FilterBaseService;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.service.filter.base.BaseFilter;
import dev.barrikeit.util.validation.SearchParams;
import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <b>Filter Base Controller Class</b>
 *
 * <p>This abstract class provides common RESTful endpoint implementations for managing entities.
 *
 * @param <E> the entity type that extends from the {@link BaseEntity}.
 * @param <D> the DTO type that extends from the {@link BaseDto}.
 * @param <F> the filter that extends from the {@link BaseFilter}.
 */
@Log4j2
public abstract class FilterBaseController<
        E extends BaseEntity, I extends Serializable, D extends BaseDto, F extends BaseFilter>
    extends BaseController<E, I, D> {

  private final FilterBaseService<E, I, D, F> filterService;

  protected FilterBaseController(FilterBaseService<E, I, D, F> filterService) {
    super(filterService);
    this.filterService = filterService;
  }

  /**
   * Método para buscar todas las entidades con un filtro.
   *
   * @param page - Información de paginación para la búsqueda.
   * @param search - Cadena de búsqueda para filtrar las entidades.
   * @return ResponseEntity con la página de resultados de la búsqueda.
   */
  @GetMapping()
  public Response<Page<D>> findAllFiltered(
      @PageableDefault(size = 20) Pageable page,
      @RequestParam(required = false, defaultValue = "") @Valid @SearchParams String search) {
    Page<D> result = filterService.search(page, search);
    Map<String, Object> meta = Map.of(
        "page", result.getNumber(),
        "size", result.getSize(),
        "totalElements", result.getTotalElements(),
        "totalPages", result.getTotalPages()
    );
    return Response.ok(result, meta);
  }
}
