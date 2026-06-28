package dev.barrikeit.service.filter.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.service.filter.specification.base.SearchCriteria;
import dev.barrikeit.service.filter.specification.base.SearchOperation;
import dev.barrikeit.util.ObjectUtil;
import dev.barrikeit.util.ReflectionUtil;
import dev.barrikeit.filter.constants.ExceptionConstants;
import dev.barrikeit.filter.constants.UtilConstants;
import dev.barrikeit.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

/**
 * Clase abstracta BaseFilterBuilder que se utiliza para construir filtros personalizados. Esta
 * clase se mapea como una superclase para las entidades que necesitan implementar la funcionalidad
 * de filtrado.
 *
 * @param <D> DTO que se utilizará para comprobar las propiedades de la clase BaseFilter para la
 *     validación de los filtros
 * @param <F> Clase que posee las propiedades que se realizarán el filtrado de los datos.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseFilterBuilder<D extends BaseDto, F extends BaseFilter> {

  private List<SearchCriteria> params = new ArrayList<>();
  private List<SearchCriteria> filters = new ArrayList<>();
  private Pageable page;
  private boolean unpaged = false;

  protected BaseFilterBuilder(Pageable page, String search) {
    this.page = page;
    String safeSearch =
        (ObjectUtils.isEmpty(search) ? "" : search) + UtilConstants.SEPARADOR_CAMPOS_BUSQUEDA;
    Pattern pattern = Pattern.compile(UtilConstants.EXPRESION_REGULAR_PARAMETROS);
    Matcher matcher = pattern.matcher(safeSearch);
    while (matcher.find()) {
      String key = matcher.group(1);
      String operator = matcher.group(2);
      String value = matcher.group(3);
      if (key.trim().equalsIgnoreCase(UtilConstants.UNPAGED_PARAMETRO_BUSQUEDA)) {
        this.unpaged = ObjectUtil.parseBoolean(value);
        continue;
      }
      with(key, operator, value);
    }
    F filter = customFilter(filters);
    validateFilter(filter);
    this.page = PageRequest.of(page.getPageNumber(), page.getPageSize(), getSort());
  }

  /**
   * Método abstracto que debe ser implementado por las subclases para construir un filtro
   * personalizado de las propiedades de la @Entity
   */
  public abstract F customFilter(List<SearchCriteria> params);

  /** Método que añade un nuevo criterio de búsqueda a la lista de criterios. */
  public BaseFilterBuilder<D, F> with(
      @NotNull final String key, @NotNull String operator, @NotNull final Object value) {
    if (ObjectUtils.isEmpty(key) || ObjectUtils.isEmpty(value)) {
      return this;
    }

    SearchOperation searchOperation = resolveSearchOperation(operator);
    SearchCriteria criteria =
        SearchCriteria.builder().key(key.trim()).operation(searchOperation).value(value).build();

    params.add(criteria);
    filters.add(criteria);
    return this;
  }

  /** Determina la operación de búsqueda a partir del operador recibido. */
  private SearchOperation resolveSearchOperation(String operator) {
    if (ObjectUtils.isEmpty(operator) || operator.length() != 1) {
      throw new BadRequestException(ExceptionConstants.ERROR_INVALID_SEARCH_OPERATION, operator);
    }

    SearchOperation op = SearchOperation.getSimpleOperation(operator.charAt(0));
    if (op == null) {
      throw new BadRequestException(ExceptionConstants.ERROR_INVALID_SEARCH_OPERATION, operator);
    }

    return op;
  }

  /** Valida que todos los criterios de búsqueda correspondan a propiedades válidas del filtro. */
  private void validateFilter(F filter) {
    List<String> validProperties =
        Arrays.stream(filter.getClass().getDeclaredFields()).map(Field::getName).toList();

    for (SearchCriteria param : params) {
      if (!validProperties.contains(param.getKey())) {
        throw new BadRequestException(ExceptionConstants.ERROR_INVALID_FILTER, param.getKey());
      }
    }
  }

  /** Obtiene el objeto Sort que se utilizará para ordenar los resultados. */
  public Sort getSort() {
    List<Sort.Order> orders = new ArrayList<>();

    List<String> sortableFields =
        ReflectionUtil.getFieldsWithAnnotation(
                ReflectionUtil.getParameterizedTypeClass(this.getClass(), 1), OrderColumn.class)
            .stream()
            .map(Field::getName)
            .toList();

    for (Sort.Order order : page.getSort()) {
      String property = order.getProperty();
      verifySortableProperty(sortableFields, property, orders, order);
    }

    return Sort.by(orders);
  }

  /** Verifica si una propiedad puede ser ordenada y la añade a la lista de órdenes si es válida. */
  private void verifySortableProperty(
      List<String> sortableFields, String property, List<Sort.Order> orders, Sort.Order order) {
    if (sortableFields.contains(property)) {
      orders.add(assignPropertyOrder(property, order));
    } else {
      throw new BadRequestException(ExceptionConstants.ERROR_INVALID_SORT, property);
    }
  }

  /** Asigna una propiedad a un objeto Sort.Order con la dirección de ordenación correspondiente. */
  public Sort.Order assignPropertyOrder(String property, Sort.Order order) {
    return new Sort.Order(order.getDirection(), property);
  }
}
