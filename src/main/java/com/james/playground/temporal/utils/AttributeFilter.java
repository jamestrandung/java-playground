package com.james.playground.temporal.utils;

import io.temporal.common.SearchAttributeKey;
import java.util.Collection;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class AttributeFilter {
  private static final String FILTER_FORMAT = "%s %s %s";
  private static final String VALUE_WRAPPER_FORMAT = "'%s'";
  private static final String BETWEEN_FORMAT = "'%s' AND '%s'";

  private AttributeFilter.AttributeFilterOperator operator;
  private String attributeName;
  private String searchValue;

  public static <T> AttributeFilter equals(
      SearchAttributeKey<T> searchAttributeKey, T searchValue
  ) {
    return simpleOperation(
        AttributeFilter.AttributeFilterOperator.EQUALS, searchAttributeKey.getName(),
        searchValue
    );
  }

  public static <T> AttributeFilter notEquals(
      SearchAttributeKey<T> searchAttributeKey, T searchValue
  ) {
    return simpleOperation(
        AttributeFilter.AttributeFilterOperator.NOT_EQUALS, searchAttributeKey.getName(),
        searchValue
    );
  }

  public static <T> AttributeFilter between(
      SearchAttributeKey<T> searchAttributeKey, T value1, T value2
  ) {
    return AttributeFilter.builder()
        .operator(AttributeFilterOperator.BETWEEN)
        .attributeName(searchAttributeKey.getName())
        .searchValue(String.format(BETWEEN_FORMAT, Objects.toString(value1, ""), Objects.toString(value2, "")))
        .build();
  }

  private static <T> AttributeFilter simpleOperation(
      AttributeFilter.AttributeFilterOperator operator, String attributeName, T searchValue
  ) {
    return AttributeFilter.builder()
        .operator(operator)
        .attributeName(attributeName)
        .searchValue(String.format(VALUE_WRAPPER_FORMAT, Objects.toString(searchValue, "")))
        .build();
  }

  public static <T> AttributeFilter in(
      SearchAttributeKey<?> searchAttributeKey, Collection<T> searchValues
  ) {
    String searchValue = StreamEx.of(CollectionUtils.emptyIfNull(searchValues))
        .map(value -> Objects.toString(value, ""))
        .map(value -> String.format(VALUE_WRAPPER_FORMAT, value))
        .joining(",");

    return AttributeFilter.builder()
        .operator(AttributeFilter.AttributeFilterOperator.IN)
        .attributeName(searchAttributeKey.getName())
        .searchValue("(" + searchValue + ")")
        .build();
  }

  public String toQueryString() {
    return String.format(FILTER_FORMAT, this.attributeName, this.operator.text, this.searchValue);
  }

  @AllArgsConstructor
  public enum AttributeFilterOperator {
    EQUALS("="),
    NOT_EQUALS("!="),
    IN("IN"),
    BETWEEN("BETWEEN");

    private final String text;
  }
}
