package dev.barrikeit.filter.constants;

/** Search-parameter parsing constants used by the generic filter framework. */
public final class UtilConstants {
  private UtilConstants() {
    throw new IllegalStateException("Constants class");
  }

  public static final String UNPAGED_PARAMETRO_BUSQUEDA = "unpaged";
  public static final String SEPARADOR_CAMPOS_BUSQUEDA = ";";
  public static final String EXPRESION_REGULAR_PARAMETROS =
      "(\\w+)([:!><])([^" + SEPARADOR_CAMPOS_BUSQUEDA + "]+)";
}
