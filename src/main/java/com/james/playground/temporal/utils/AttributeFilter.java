package com.james.playground.temporal.utils;

import java.util.Collection;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import one.util.streamex.StreamEx;
import org.apache.commons.collections4.CollectionUtils;

@Data
@Builder(access = AccessLevel.PRIVATE)
public class AttributeFilter {
  private static final String FILTER_FORMAT = "%s %s %s";
  private static final String VALUE_WRAPPER_FORMAT = "'%s'";

  private AttributeFilterOperator operator;
  private String attributeName;
  private String searchValue;

  public static <T> AttributeFilter create(String attributeName, T searchValue) {
    return AttributeFilter.builder()
        .operator(AttributeFilterOperator.EQUALS)
        .attributeName(attributeName)
        .searchValue(String.format(VALUE_WRAPPER_FORMAT, Objects.toString(searchValue, "")))
        .build();
  }

  public static <T> AttributeFilter create(String attributeName, Collection<T> searchValues) {
    String searchValue = StreamEx.of(CollectionUtils.emptyIfNull(searchValues))
        .map(value -> Objects.toString(value, ""))
        .map(value -> String.format(VALUE_WRAPPER_FORMAT, value))
        .joining(",");

    return AttributeFilter.builder()
        .operator(AttributeFilterOperator.IN)
        .searchValue("(" + searchValue + ")")
        .build();
  }

  public String toString() {
    return String.format(FILTER_FORMAT, this.attributeName, this.operator.text, this.searchValue);
  }

  @AllArgsConstructor
  public enum AttributeFilterOperator {
    EQUALS("="),
    NOT_EQUALS("!="),
    IN("IN");

    private final String text;
  }
}
