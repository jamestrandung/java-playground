package com.james.playground.temporal.dsl.workflows.visitors;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.DelayVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.PrinterVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.RandomDistributionVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.SwitchVisitor;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.TransitVisitor;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
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
  private final SwitchVisitor switchVisitor;

  public DelegatingVisitor(
      DynamicWorkflowInput input,
      Supplier<WorkflowDefinition<?>> workflowDefinitionSupplier
  ) {
    this.transitVisitor = new TransitVisitor(input);
    this.delayVisitor = new DelayVisitor(input, workflowDefinitionSupplier);
    this.printerVisitor = new PrinterVisitor(input);
    this.randomDistributionVisitor = new RandomDistributionVisitor(input);
    this.switchVisitor = new SwitchVisitor(input);
  }
}
