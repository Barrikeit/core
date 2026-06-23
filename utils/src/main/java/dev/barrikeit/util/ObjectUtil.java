package dev.barrikeit.util;

import dev.barrikeit.data.dto.BaseDto;
import dev.barrikeit.data.entity.BaseEntity;
import dev.barrikeit.exception.UnexpectedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

public final class ObjectUtil {

  private ObjectUtil() {
    throw new IllegalStateException("ObjectUtil class");
  }

  public static boolean isEntityOrDto(Class<?> clazz) {
    return BaseEntity.class.isAssignableFrom(clazz) || BaseDto.class.isAssignableFrom(clazz);
  }

  public static boolean isSimpleType(Class<?> type) {
    return type.isPrimitive()
        || type.isEnum()
        || type.equals(String.class)
        || Number.class.isAssignableFrom(type)
        || Boolean.class.isAssignableFrom(type)
        || Date.class.isAssignableFrom(type)
        || type.equals(LocalDate.class)
        || type.equals(LocalDateTime.class)
        || type.equals(OffsetDateTime.class);
  }

  public static boolean isEmpty(Object value) {
    if (value == null) return true;
    if (value instanceof String s) return s.isBlank();
    return false;
  }

  public static boolean parseBoolean(String value) {
    if (isEmpty(value)) return false;
    String normalized = value.trim().toLowerCase();
    return switch (normalized) {
      case "true", "1" -> true;
      case "false", "0" -> false;
      default -> throw new UnexpectedException("Unsupported boolean value: %s", value);
    };
  }

  @SuppressWarnings("unchecked")
  public static <M> M castToType(Object value, Class<M> targetType) {
    if (value == null) return null;

    try {
      if (targetType.isInstance(value)) return (M) value;
      if (targetType.equals(String.class)) return (M) value.toString();
      if (targetType.equals(Integer.class) || targetType.equals(int.class))
        return (M) Integer.valueOf(value.toString());
      if (targetType.equals(Long.class) || targetType.equals(long.class))
        return (M) Long.valueOf(value.toString());
      if (targetType.equals(Float.class) || targetType.equals(float.class))
        return (M) Float.valueOf(value.toString());
      if (targetType.equals(Double.class) || targetType.equals(double.class))
        return (M) Double.valueOf(value.toString());

      if (targetType.equals(BigDecimal.class)) {
        if (value instanceof String s) return (M) new BigDecimal(s);
        if (value instanceof Long l) return (M) BigDecimal.valueOf(l);
        if (value instanceof Double d) return (M) BigDecimal.valueOf(d);
      }

      if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
        if (value instanceof String s) return (M) Boolean.valueOf(s);
      }

      if (targetType.equals(OffsetDateTime.class)) {
        if (value instanceof String s) return (M) TimeUtil.parseOffsetDateTime(s);
        if (value instanceof LocalDate d) return (M) TimeUtil.toOffsetDateTime(d);
        if (value instanceof LocalDateTime dt) return (M) TimeUtil.toOffsetDateTime(dt);
      }

      if (targetType.equals(LocalDate.class)) {
        if (value instanceof String s) return (M) TimeUtil.parseOffsetDateTime(s).toLocalDate();
        if (value instanceof OffsetDateTime odt) return (M) odt.toLocalDate();
        if (value instanceof LocalDateTime dt) return (M) dt.toLocalDate();
      }

      if (targetType.equals(LocalDateTime.class)) {
        if (value instanceof String s) return (M) TimeUtil.parseOffsetDateTime(s).toLocalDateTime();
        if (value instanceof LocalDate d) return (M) d.atStartOfDay();
        if (value instanceof OffsetDateTime odt) return (M) odt.toLocalDateTime();
      }

    } catch (Exception e) {
      throw new UnexpectedException(
          "Failed to cast value '%s' to type '%s': %s",
          value, targetType.getName(), e.getMessage());
    }

    throw new UnexpectedException("Unsupported target type: %s", targetType.getName());
  }
}
