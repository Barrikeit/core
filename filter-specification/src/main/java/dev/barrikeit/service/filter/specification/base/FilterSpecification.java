package dev.barrikeit.service.filter.specification.base;

import dev.barrikeit.util.TimeUtil;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@Builder
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class FilterSpecification<E> implements Specification<E> {

  private final SearchCriteria searchCriteria;

  private Map<SearchOperation, BiFunction<Root<E>, CriteriaBuilder, Predicate>>
      operationPredicateMap = new HashMap<>();

  public FilterSpecification(SearchCriteria searchCriteria) {
    this.searchCriteria = searchCriteria;
    operationPredicateMap.put(SearchOperation.EQUALITY, this::equalityPredicate);
    operationPredicateMap.put(SearchOperation.NEGATION, this::negationPredicate);
    operationPredicateMap.put(SearchOperation.GREATER_THAN, this::greaterThanPredicate);
    operationPredicateMap.put(SearchOperation.LESS_THAN, this::lessThanPredicate);
    operationPredicateMap.put(SearchOperation.LIKE, this::equalityPredicate);
    operationPredicateMap.put(SearchOperation.IS_NULL, this::isNullPredicate);
    operationPredicateMap.put(SearchOperation.IS_NOT_NULL, this::isNotNullPredicate);
  }

  @Override
  public Predicate toPredicate(
      Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    if (null == this.searchCriteria) {
      return null;
    }
    return operationPredicateMap
        .getOrDefault(searchCriteria.getOperation(), (r, c) -> null)
        .apply(root, criteriaBuilder);
  }

  private Predicate equalityPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    if (root.get(this.searchCriteria.getKey()).getJavaType() == String.class) {
      Expression<String> fieldExpr =
          criteriaBuilder.lower(
              criteriaBuilder.function(
                  "unaccent", String.class, root.get(searchCriteria.getKey())));

      Expression<String> valueExpr =
          criteriaBuilder.function(
              "unaccent",
              String.class,
              criteriaBuilder.literal(
                  "%" + ((String) searchCriteria.getValue()).toLowerCase() + "%"));

      return criteriaBuilder.like(fieldExpr, valueExpr);
    } else {
      return criteriaBuilder.equal(
          root.get(this.searchCriteria.getKey()), this.searchCriteria.getValue());
    }
  }

  private Predicate negationPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    if (root.get(this.searchCriteria.getKey()).getJavaType() == String.class) {
      Expression<String> fieldExpr =
          criteriaBuilder.lower(
              criteriaBuilder.function(
                  "unaccent", String.class, root.get(searchCriteria.getKey())));

      Expression<String> valueExpr =
          criteriaBuilder.function(
              "unaccent",
              String.class,
              criteriaBuilder.literal(
                  "%" + ((String) searchCriteria.getValue()).toLowerCase() + "%"));

      return criteriaBuilder.notLike(fieldExpr, valueExpr);
    } else {
      return criteriaBuilder.notEqual(
          root.get(this.searchCriteria.getKey()), this.searchCriteria.getValue());
    }
  }

  private Predicate greaterThanPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    Class<?> javaType = root.get(this.searchCriteria.getKey()).getJavaType();
    if (javaType == OffsetDateTime.class) {
      OffsetDateTime value =
          searchCriteria.getValue() instanceof String s
              ? TimeUtil.parseOffsetDateTime(s)
              : (OffsetDateTime) searchCriteria.getValue();
      return criteriaBuilder.greaterThanOrEqualTo(root.get(this.searchCriteria.getKey()), value);
    } else if (javaType == LocalDate.class) {
      LocalDate localDate =
          searchCriteria.getValue() instanceof String s
              ? TimeUtil.parseOffsetDateTime(s).toLocalDate()
              : (LocalDate) searchCriteria.getValue();
      return criteriaBuilder.greaterThanOrEqualTo(
          root.get(this.searchCriteria.getKey()), localDate);
    } else {
      return criteriaBuilder.greaterThanOrEqualTo(
          root.get(this.searchCriteria.getKey()), searchCriteria.getValue().toString());
    }
  }

  private Predicate lessThanPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    Class<?> javaType = root.get(this.searchCriteria.getKey()).getJavaType();
    if (javaType == OffsetDateTime.class) {
      OffsetDateTime value =
          searchCriteria.getValue() instanceof String s
              ? TimeUtil.parseOffsetDateTime(s)
              : (OffsetDateTime) searchCriteria.getValue();
      return criteriaBuilder.lessThanOrEqualTo(root.get(this.searchCriteria.getKey()), value);
    } else if (javaType == LocalDate.class) {
      LocalDate localDate =
          searchCriteria.getValue() instanceof String s
              ? TimeUtil.parseOffsetDateTime(s).toLocalDate()
              : (LocalDate) searchCriteria.getValue();
      return criteriaBuilder.lessThanOrEqualTo(root.get(this.searchCriteria.getKey()), localDate);
    } else {
      return criteriaBuilder.lessThanOrEqualTo(
          root.get(this.searchCriteria.getKey()), this.searchCriteria.getValue().toString());
    }
  }

  private Predicate isNullPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    return criteriaBuilder.isNull(root.get(this.searchCriteria.getKey()));
  }

  private Predicate isNotNullPredicate(Root<E> root, CriteriaBuilder criteriaBuilder) {
    return criteriaBuilder.isNotNull(root.get(this.searchCriteria.getKey()));
  }
}
