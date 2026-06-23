package dev.barrikeit.util;

import dev.barrikeit.exception.FieldValueException;
import dev.barrikeit.exception.UnexpectedException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reflection utilities. Pure JDK — no Spring ReflectionUtils, no Apache Commons Lang3, no external
 * dependencies.
 */
public final class ReflectionUtil {

  private ReflectionUtil() {
    throw new IllegalStateException("ReflectionUtil class");
  }

  /**
   * Creates a new instance of a class using its no-arg constructor. Returns null if the class has
   * no accessible no-arg constructor.
   */
  public static Object newInstance(Class<?> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      return null;
    }
  }

  /**
   * Gets the value of a field by invoking its getter method.
   *
   * @throws FieldValueException if the getter cannot be invoked
   */
  public static Object getFieldValue(Object instance, String fieldName) {
    String getterName = "get" + capitalize(fieldName);
    Method getter = findMethod(instance.getClass(), getterName);
    if (getter == null) return null;
    try {
      return getter.invoke(instance);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new FieldValueException(
          "Failed to get field '%s' on %s: %s",
          fieldName, instance.getClass().getSimpleName(), e.getMessage());
    }
  }

  /**
   * Sets the value of a field by invoking its setter method.
   *
   * @throws FieldValueException if the setter cannot be invoked
   */
  public static void setFieldValue(Object instance, String fieldName, Object value) {
    if (value == null) return;
    String setterName = "set" + capitalize(fieldName);
    Method setter = findMethod(instance.getClass(), setterName, value.getClass());
    if (setter == null) return;
    try {
      setter.invoke(instance, value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new FieldValueException(
          "Failed to set field '%s' on %s: %s",
          fieldName, instance.getClass().getSimpleName(), e.getMessage());
    }
  }

  /**
   * Returns the class of the generic type parameter at the given index from the direct superclass
   * of {@code clazz}.
   */
  @SuppressWarnings("unchecked")
  public static <E> Class<E> getParameterizedTypeClass(Class<E> clazz, int index) {
    ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
    Type[] typeArguments = parameterizedType.getActualTypeArguments();
    return (Class<E>) typeArguments[index];
  }

  /**
   * Walks the class hierarchy to find the first parameterized superclass, then returns the type
   * argument at the given index. Supports direct types (e.g. {@code DTO}) and collection types
   * (e.g. {@code List<DTO>}).
   *
   * @throws IllegalStateException if the generic type cannot be resolved
   */
  public static Class<?> getSuperClass(Class<?> clazz, int paramIndex) {
    Type type = clazz.getGenericSuperclass();

    while (!(type instanceof ParameterizedType) && clazz.getSuperclass() != null) {
      clazz = clazz.getSuperclass();
      type = clazz.getGenericSuperclass();
    }

    if (type instanceof ParameterizedType parameterizedType) {
      Type actualType = parameterizedType.getActualTypeArguments()[paramIndex];

      // Collection type e.g. List<DTO>, Set<DTO>
      if (actualType instanceof ParameterizedType pt) {
        Type rawType = pt.getRawType();
        if (rawType instanceof Class<?> rawClass && Collection.class.isAssignableFrom(rawClass)) {
          Type innerType = pt.getActualTypeArguments()[0];
          if (innerType instanceof Class<?> innerClass) return innerClass;
        }
      }

      // Direct type e.g. DTO
      if (actualType instanceof Class<?> directClass) return directClass;
    }

    throw new IllegalStateException(
        "Cannot resolve generic type at index " + paramIndex + " for " + clazz.getName());
  }

  /**
   * Returns all declared fields of a class and its superclasses, excluding static fields and fields
   * marked with the {@code transient} modifier.
   *
   * <p>Note: replaces the {@code @jakarta.persistence.Transient} annotation filter from the
   * original — use the {@code transient} Java keyword on fields you want excluded here.
   */
  public static List<Field> getFields(Class<?> clazz) {
    List<Field> fields =
        new ArrayList<>(
            Arrays.stream(clazz.getDeclaredFields())
                .filter(
                    f ->
                        !Modifier.isTransient(f.getModifiers())
                            && !Modifier.isStatic(f.getModifiers()))
                .toList());

    Class<?> superClass = clazz.getSuperclass();
    if (superClass != null && superClass != Object.class) {
      fields.addAll(getFields(superClass));
    }
    return fields;
  }

  /** Returns all fields of a class (including superclasses) annotated with the given annotation. */
  public static List<Field> getFieldsWithAnnotation(
      Class<?> clazz, Class<? extends Annotation> annotation) {
    return getFields(clazz).stream().filter(f -> f.isAnnotationPresent(annotation)).toList();
  }

  /**
   * Returns all fields of a class (including superclasses) NOT annotated with the given annotation.
   */
  public static List<Field> getFieldsWithoutAnnotation(
      Class<?> clazz, Class<? extends Annotation> annotation) {
    return getFields(clazz).stream().filter(f -> !f.isAnnotationPresent(annotation)).toList();
  }

  /**
   * Returns a map of dot-notation field names to Field objects, recursing into entity/DTO types.
   */
  public static Map<String, Field> getNestedFields(Class<?> clazz, String prefix) {
    return getFields(clazz).stream()
        .flatMap(
            field -> {
              String fullName = buildFullFieldName(prefix, field.getName());
              if (ObjectUtil.isEntityOrDto(field.getDeclaringClass())) {
                return getNestedFields(field.getType(), fullName).entrySet().stream();
              }
              return Stream.of(Map.entry(fullName, field));
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Returns a map of field name to Field for all annotated fields.
   *
   * @throws UnexpectedException if no annotated fields are found
   */
  public static Map<String, Field> getAnnotatedFields(
      Class<?> clazz, Class<? extends Annotation> annotation) {
    List<Field> fields = getFieldsWithAnnotation(clazz, annotation);
    if (fields.isEmpty()) {
      throw new UnexpectedException(
          "No fields annotated with @%s found on %s",
          annotation.getSimpleName(), clazz.getSimpleName());
    }
    return fields.stream().collect(Collectors.toMap(Field::getName, f -> f));
  }

  /**
   * Returns annotated nested fields as a dot-notation name → Field map.
   *
   * @throws UnexpectedException if no annotated fields are found
   */
  public static Map<String, Field> getAnnotatedNestedFields(
      Class<?> clazz, Class<? extends Annotation> annotation, String fieldName) {
    return getFieldsWithAnnotation(clazz, annotation).stream()
        .flatMap(
            field -> {
              String fullName = buildFullFieldName(fieldName, field.getName());
              if (ObjectUtil.isEntityOrDto(field.getDeclaringClass())) {
                return getAnnotatedNestedFields(field.getDeclaringClass(), annotation, fullName)
                    .entrySet()
                    .stream();
              }
              return Stream.of(Map.entry(fullName, field));
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Returns the properties of an annotation on a class as a name → value map.
   *
   * @throws UnexpectedException if the annotation is not present on the class
   * @throws FieldValueException if a property method cannot be invoked
   */
  public static Map<String, Object> getAnnotationProperties(
      Class<?> clazz, Class<? extends Annotation> annotation) {
    if (!clazz.isAnnotationPresent(annotation)) {
      throw new UnexpectedException(
          "Annotation @%s not found on %s", annotation.getSimpleName(), clazz.getSimpleName());
    }

    Map<String, Object> properties = new HashMap<>();
    Annotation instance = clazz.getAnnotation(annotation);

    for (Method method : annotation.getDeclaredMethods()) {
      try {
        properties.put(method.getName(), method.invoke(instance));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new FieldValueException(
            "Failed to read @%s.%s on %s: %s",
            annotation.getSimpleName(), method.getName(), clazz.getSimpleName(), e.getMessage());
      }
    }
    return properties;
  }

  /** Returns a list of field values from an instance in the order of the given field list. */
  public static List<Object> getListFieldValues(Object instance, List<Field> fields) {
    List<Object> values = new ArrayList<>();
    for (Field field : fields) {
      values.add(getFieldValue(instance, field.getName()));
    }
    return values;
  }

  /**
   * Returns a dot-notation name → value map for a set of fields on an instance. Recurses into
   * entity/DTO nested types, arrays, collections, and maps.
   */
  public static Map<String, Object> getMapFieldValues(Object instance, Map<String, Field> fields) {
    Map<String, Object> values = new LinkedHashMap<>();

    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      String fieldName = entry.getKey();
      Field field = entry.getValue();
      Object value = getFieldValue(instance, field.getName());

      if (value == null) {
        values.put(fieldName, null);
        continue;
      }

      Class<?> type = value.getClass();

      if (ObjectUtil.isEntityOrDto(type)) {
        values.putAll(getMapFieldValues(value, getNestedFields(value.getClass(), fieldName)));

      } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
        Collection<?> collection =
            type.isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
        int index = 0;
        for (Object item : collection) {
          if (item == null) continue;
          String indexedKey = fieldName + ".[" + index + "]";
          if (ObjectUtil.isSimpleType(item.getClass())) {
            values.put(indexedKey, item);
          } else if (ObjectUtil.isEntityOrDto(item.getClass())) {
            values.putAll(getMapFieldValues(item, getNestedFields(item.getClass(), indexedKey)));
          } else {
            throw new UnexpectedException(
                "Nested collection inside collection not supported: %s", indexedKey);
          }
          index++;
        }

      } else if (Map.class.isAssignableFrom(type)) {
        Map<?, ?> map = (Map<?, ?>) value;
        for (Map.Entry<?, ?> mapEntry : map.entrySet()) {
          String key = String.valueOf(mapEntry.getKey());
          Object val = mapEntry.getValue();
          String mappedKey = fieldName + ".[" + key + "]";
          if (val == null) continue;
          if (ObjectUtil.isSimpleType(val.getClass())) {
            values.put(mappedKey, val);
          } else if (ObjectUtil.isEntityOrDto(val.getClass())) {
            values.putAll(getMapFieldValues(val, getNestedFields(val.getClass(), mappedKey)));
          } else {
            throw new UnexpectedException("Nested map inside map not supported: %s", mappedKey);
          }
        }

      } else {
        values.put(fieldName, value);
      }
    }
    return values;
  }

  /**
   * Returns a flat dot-notation name → value map for all fields on an instance, recursing into
   * entity/DTO, array, collection, and map types.
   */
  public static Map<String, Object> getNestedFieldValues(Object instance, String prefix) {
    Map<String, Object> values = new HashMap<>();

    for (Field field : getFields(instance.getClass())) {
      Object value = getFieldValue(instance, field.getName());
      String fieldName = buildFullFieldName(prefix, field.getName());

      if (value == null) {
        values.put(fieldName, null);
        continue;
      }

      Class<?> type = value.getClass();

      if (ObjectUtil.isEntityOrDto(type)) {
        values.putAll(getNestedFieldValues(value, fieldName));

      } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
        Collection<?> collection =
            type.isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
        if (collection.isEmpty()) {
          values.put(fieldName, null);
        } else {
          int index = 0;
          for (Object item : collection) {
            String indexedKey = fieldName + ".[" + index + "]";
            if (ObjectUtil.isSimpleType(item.getClass())) {
              values.put(indexedKey, item);
            } else {
              values.putAll(getNestedFieldValues(item, indexedKey));
            }
            index++;
          }
        }

      } else if (Map.class.isAssignableFrom(type)) {
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.isEmpty()) values.put(fieldName, null);
        for (Map.Entry<?, ?> mapEntry : map.entrySet()) {
          String key = String.valueOf(mapEntry.getKey());
          Object item = mapEntry.getValue();
          String mappedKey = fieldName + ".[" + key + "]";
          if (ObjectUtil.isSimpleType(item.getClass())) {
            values.put(mappedKey, item);
          } else {
            values.putAll(getNestedFieldValues(item, mappedKey));
          }
        }

      } else {
        values.put(fieldName, value);
      }
    }
    return values;
  }

  public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      try {
        return paramTypes.length > 0
            ? current.getDeclaredMethod(name, paramTypes)
            : current.getDeclaredMethod(name);
      } catch (NoSuchMethodException e) {
        // try superclass
      }
      // also check interfaces for default methods
      for (Class<?> iface : current.getInterfaces()) {
        try {
          return iface.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException ignored) {
        }
      }
      current = current.getSuperclass();
    }
    return null;
  }

  private static String capitalize(String name) {
    return name.substring(0, 1).toUpperCase() + name.substring(1);
  }

  private static String buildFullFieldName(String parent, String child) {
    return (parent == null || parent.isBlank()) ? child : parent + "." + child;
  }
}
