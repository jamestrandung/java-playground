package com.james.playground.temporal.utils;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public class WorkflowFilter {
  private static final String FILTER_FORMAT = "(%s) %s (%s)";

  private WorkflowFilterOperator operator;

  private AttributeFilter attributeFilter;
  private WorkflowFilter left;
  private WorkflowFilter right;

  public static WorkflowFilter basic(AttributeFilter attributeFilter) {
    return WorkflowFilter.builder()
        .operator(WorkflowFilterOperator.NONE)
        .attributeFilter(attributeFilter)
        .build();
  }

  public WorkflowFilter and(WorkflowFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilterOperator.AND)
        .left(this)
        .right(right)
        .build();
  }

  public WorkflowFilter and(AttributeFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilterOperator.AND)
        .left(this)
        .right(WorkflowFilter.basic(right))
        .build();
  }

  public WorkflowFilter or(WorkflowFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilterOperator.OR)
        .left(this)
        .right(right)
        .build();
  }

  public WorkflowFilter or(AttributeFilter right) {
    if (right == null) {
      return this;
    }

    return WorkflowFilter.builder()
        .operator(WorkflowFilterOperator.OR)
        .left(this)
        .right(WorkflowFilter.basic(right))
        .build();
  }

  public String toString() {
    if (this.operator == WorkflowFilterOperator.NONE) {
      return this.attributeFilter.toString();
    }

    return String.format(FILTER_FORMAT, this.left, this.operator.name(), this.right);
  }

  public enum WorkflowFilterOperator {
    NONE, AND, OR
  }
}
