package dev.barrikeit.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import dev.barrikeit.filter.constants.UtilConstants;

public class SearchParamsValidator implements ConstraintValidator<SearchParams, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Si la cadena es un espacio en blanco o esta vacia la cadena es valida.
    if (value == null || value.isBlank()) {
      return true;
    }

    // En cualquier otro caso valida el contenido de la cadena
    return validarCadena(value);
  }

  private boolean validarParametro(String value) {
    return value.matches(UtilConstants.EXPRESION_REGULAR_PARAMETROS);
  }

  private boolean validarCadena(String value) {
    String[] array = value.split(UtilConstants.SEPARADOR_CAMPOS_BUSQUEDA);
    boolean result = true;
    for (String s : array) {
      if (!validarParametro(s)) {
        result = false;
        break;
      }
    }
    return result;
  }
}
