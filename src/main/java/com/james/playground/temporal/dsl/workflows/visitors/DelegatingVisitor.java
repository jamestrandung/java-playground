package com.james.playground.temporal.dsl.workflows.visitors;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.BranchVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.DelayVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.PrinterVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.RandomDistributionVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.TransitVisitor;
import lombok.experimental.Delegate;

public class DelegatingVisitor {
  @Delegate
  private final TransitVisitor transitVisitor;
  @Delegate
  private final PrinterVisitor printerVisitor;
  @Delegate
  private final DelayVisitor delayVisitor;
  @Delegate
  private final RandomDistributionVisitor randomDistributionVisitor;
  @Delegate
  private final BranchVisitor branchVisitor;

  public DelegatingVisitor(DynamicWorkflowInput input) {
    this.transitVisitor = new TransitVisitor(input);
    this.delayVisitor = new DelayVisitor(input);
    this.printerVisitor = new PrinterVisitor(input);
    this.randomDistributionVisitor = new RandomDistributionVisitor(input);
    this.branchVisitor = new BranchVisitor(input);
  }
}
