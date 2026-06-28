package dev.barrikeit.service.base;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import dev.barrikeit.springframework.data.entity.BaseEntity;
import dev.barrikeit.springframework.data.repository.BaseRepository;
import dev.barrikeit.springframework.data.repository.FilterBaseRepository;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.service.filter.base.BaseFilter;
import dev.barrikeit.service.filter.base.BaseFilterBuilder;
import dev.barrikeit.service.filter.specification.base.FilterSpecification;
import dev.barrikeit.service.filter.specification.base.SearchCriteria;
import dev.barrikeit.data.mapper.BaseMapper;
import dev.barrikeit.util.ReflectionUtil;
import dev.barrikeit.filter.constants.ExceptionConstants;
import dev.barrikeit.exception.BadRequestException;
import dev.barrikeit.exception.NoSuchMethodException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

/**
 * <b>Filter Base Service Class</b>
 *
 * <p>This abstract class provides a basic implementation of service operations for filtering
 * entities and their corresponding DTOs. It interacts with a {@link FilterBaseRepository} for data
 * access and uses a {@link BaseMapper} for object mapping between entities and DTOs.
 *
 * @param <E> the entity type that extends {@link BaseEntity}.
 * @param <I> the type of the entity's identifier, which must be {@link Serializable}.
 * @param <D> the DTO type that extends {@link BaseDto}.
 * @param <F> the Filter {@link BaseFilter}.
 */
@Log4j2
public class FilterBaseService<
        E extends BaseEntity, I extends Serializable, D extends BaseDto, F extends BaseFilter>
    extends BaseService<E, I, D> implements FilterableService<D, F> {

  private final BaseRepository<E, I> repository;
  private final BaseMapper<E, D> mapper;

  protected FilterBaseService(BaseRepository<E, I> repository, BaseMapper<E, D> mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  private static <E extends BaseEntity> Specification<E> getSpecification(SearchCriteria param) {
    return new FilterSpecification<>(param) {
      @Override
      public Predicate toPredicate(
          Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (null == getSearchCriteria()) {
          return null;
        }
        String[] parts = getSearchCriteria().getKey().split("\\.");
        Join<E, Object> join = root.join(parts[0], JoinType.INNER);

        String attributeValue = getSearchCriteria().getValue().toString();

        return criteriaBuilder.like(
            criteriaBuilder.lower(join.get(parts[1]).as(String.class)),
            "%" + attributeValue.toLowerCase() + "%");
      }
    };
  }

  @Override
  public BaseFilterBuilder<D, F> instanceFilterBuilder(Pageable page, String search) {
    throw new NoSuchMethodException(
        ExceptionConstants.ERROR_NO_SUCH_MERTHOD,
        "instanceFilterBuilder",
        getClass().getSimpleName());
  }

  /**
   * Realiza una búsqueda paginada o no paginada en base a los criterios de búsqueda proporcionados.
   *
   * @param page El objeto Pageable que contiene la información de paginación.
   * @param search Una cadena de texto que contiene los criterios de búsqueda.
   * @return Una página de DTOs que cumplen con los criterios de búsqueda.
   */
  public Page<D> search(@NotNull Pageable page, String search) {
    Page<E> paged = searchEntity(page, search);
    List<D> result = paged.getContent().stream().map(mapper::toDto).toList();
    return new PageImpl<>(result, paged.getPageable(), paged.getTotalElements());
  }

  /**
   * Realiza una búsqueda paginada o no paginada en base a los criterios de búsqueda proporcionados.
   *
   * @param page El objeto Pageable que contiene la información de paginación.
   * @param search Una cadena de texto que contiene los criterios de búsqueda.
   * @return Una página de Entidades que cumplen con los criterios de búsqueda.
   */
  public Page<E> searchEntity(@NotNull Pageable page, String search) {
    BaseFilterBuilder<D, F> filterBuilder = instanceFilterBuilder(page, search);
    return filterBuilder.isUnpaged()
        ? searchEntityUnpaged(filterBuilder)
        : repository.findAll(createSpecificationField(filterBuilder), filterBuilder.getPage());
  }

  /**
   * Realiza una búsqueda no paginada y devuelve los resultados.
   *
   * @param filterBuilder El constructor de filtros que contiene los criterios de búsqueda.
   * @return Una página de Entidades que cumplen con los criterios de búsqueda.
   */
  private Page<E> searchEntityUnpaged(BaseFilterBuilder<D, F> filterBuilder) {
    List<E> result =
        repository
            .findAll(createSpecificationField(filterBuilder), filterBuilder.getPage().getSort())
            .stream()
            .toList();
    return new PageImpl<>(result, filterBuilder.getPage(), result.size());
  }

  /**
   * Crea una especificación de búsqueda en base a los filtros proporcionados.
   *
   * @param filterBuilder El constructor de filtros que contiene los criterios de búsqueda.
   * @return Una especificación que puede ser usada para realizar una búsqueda en el repositorio.
   */
  public Specification<E> createSpecificationField(BaseFilterBuilder<D, F> filterBuilder) {
    List<Specification<E>> conditions = new ArrayList<>();

    if (ObjectUtils.isEmpty(filterBuilder.getFilters())) {
      return null;
    }

    // Se recorren los filtros y se añaden las especificaciones correspondientes
    for (SearchCriteria param : filterBuilder.getFilters()) {
      if (ReflectionUtil.getFields(ReflectionUtil.getParameterizedTypeClass(this.getClass(), 0))
          .stream()
          .noneMatch(f -> param.getKey().startsWith(f.getName()))) {
        throw new BadRequestException(ExceptionConstants.ERROR_INVALID_FILTER, param.getKey());
      }

      if (!ObjectUtils.isEmpty(param.getValue())) {
        // Si la clave contiene un punto y la especificación es nula, se crea una especificación
        if (param.getKey().contains(".") && param.getSpecification() == null) {
          Specification<E> specification = getSpecification(param);
          conditions.add(specification);
        }
        // Si la clave no contiene un punto y la especificación no es nula, se añade la
        // especificación
        else if (param.getSpecification() != null) {
          @SuppressWarnings("unchecked")
          Specification<E> specification = (Specification<E>) param.getSpecification();
          conditions.add(specification);
        }
        // Si la clave no contiene un punto y la especificación es nula, se añade un filtro
        else {
          conditions.add(new FilterSpecification<>(param));
        }
      }
    }

    return conditions.stream().reduce(Specification::and).orElse(null);
  }
}
