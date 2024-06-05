package com.james.playground.temporal.dsl.language.nodes;

import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.DynamicWorkflowImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrinterNode extends WorkflowNode {
  private String text;

  @Override
  public WorkflowNode accept(DynamicWorkflowImpl visitor) {
    return visitor.visit(this);
  }
}
