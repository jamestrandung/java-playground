package com.james.playground.temporal.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class WorkflowFilter {
  private static final String FILTER_FORMAT = "(%s) %s (%s)";

  private WorkflowFilter.WorkflowFilterOperator operator;

  private AttributeFilter attributeFilter;
  private WorkflowFilter left;
  private WorkflowFilter right;

  public static WorkflowFilter basic(
      AttributeFilter attributeFilter
  ) {
    return WorkflowFilter.builder()
        .operator(WorkflowFilter.WorkflowFilterOperator.NONE)
        .attributeFilter(attributeFilter)
        .build();
  }

  public WorkflowFilter and(WorkflowFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilter.WorkflowFilterOperator.AND)
        .left(this)
        .right(right)
        .build();
  }

  public WorkflowFilter and(AttributeFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilter.WorkflowFilterOperator.AND)
        .left(this)
        .right(WorkflowFilter.basic(right))
        .build();
  }

  public WorkflowFilter or(WorkflowFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilter.WorkflowFilterOperator.OR)
        .left(this)
        .right(right)
        .build();
  }

  public WorkflowFilter or(AttributeFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilter.WorkflowFilterOperator.OR)
        .left(this)
        .right(WorkflowFilter.basic(right))
        .build();
  }

  public String toQueryString() {
    if (this.operator == WorkflowFilter.WorkflowFilterOperator.NONE) {
      return this.attributeFilter.toQueryString();
    }

    return String.format(FILTER_FORMAT, this.left.toQueryString(), this.operator.name(), this.right.toQueryString());
  }

  public enum WorkflowFilterOperator {
    NONE, AND, OR
  }
}

