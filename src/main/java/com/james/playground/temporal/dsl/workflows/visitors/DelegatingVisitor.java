package com.james.playground.temporal.dsl.workflows.visitors;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import lombok.experimental.Delegate;

public class DelegatingVisitor extends BaseVisitor {
  @Delegate
  private final TransitVisitor transitVisitor;
  @Delegate
  private final PrinterVisitor printerVisitor;
  @Delegate
  private final DelayVisitor delayVisitor;

  public DelegatingVisitor(DynamicWorkflowInput input) {
    super(input);
    this.transitVisitor = new TransitVisitor(input);
    this.delayVisitor = new DelayVisitor(input);
    this.printerVisitor = new PrinterVisitor(input);
  }
}
