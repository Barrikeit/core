package dev.barrikeit.service.filter.specification.base;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

/**
 * La clase SearchCriteria representa los criterios de búsqueda que se utilizarán para filtrar los
 * resultados de una consulta. Cada instancia de SearchCriteria contiene una clave (el nombre del
 * campo que se va a buscar), una operación (la operación que se va a realizar, como igual, mayor
 * que, menor que, etc.) y un valor (el valor que se va a comparar con el valor del campo). También
 * puede contener una especificación, que es una especificación de JPA que se puede utilizar para
 * realizar consultas más complejas.
 */
@Builder
@Getter
@Setter
public class SearchCriteria implements Serializable {

  /** La clave es el nombre del campo que se va a buscar. */
  private String key;

  /** La operación es la operación que se va a realizar, como igual, mayor que, menor que, etc. */
  private SearchOperation operation;

  /** El valor es el valor que se va a comparar con el valor del campo. */
  private Object value;

  /**
   * La especificación es una especificación de JPA que se puede utilizar para realizar consultas
   * más complejas.
   */
  private Specification<?> specification;
}
