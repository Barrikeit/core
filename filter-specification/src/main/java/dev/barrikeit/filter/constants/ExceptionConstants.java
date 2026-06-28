package dev.barrikeit.filter.constants;

/** Error message keys used by the generic CRUD / filter framework. */
public final class ExceptionConstants {
  private ExceptionConstants() {
    throw new IllegalStateException("Constants class");
  }

  public static final String NOT_FOUND = "Not found Exception";
  public static final String ERROR_NO_SUCH_MERTHOD = "Method {0} doesn't exist for class {1}";
  public static final String ERROR_INVALID_SEARCH_OPERATION =
      "{0} es un operador no válido para los parámetros de la búsqueda";
  public static final String ERROR_INVALID_FILTER = "{0} no es un filtro de la búsqueda válido";
  public static final String ERROR_INVALID_SORT = "{0} no es un criterio de ordenación válido";
}
