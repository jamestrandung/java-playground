package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.NodeType.Constants;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.util.Optional;
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
  @JsonProperty(Constants.PROPERTY_NAME)
  private final NodeType type = NodeType.PRINTER;

  private String text;

  @Override
  public String accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public Optional<NodeChangeSignal> detectChange(WorkflowNode latest) {
    return Optional.empty();
  }

  @Override
  protected boolean canBeDeleted() {
    return true;
  }
}
