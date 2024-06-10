package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransitNode extends WorkflowNode {
  public static final String START_ID = "START";
  public static final String END_ID = "END";

  @JsonIgnore
  public boolean isEndNode() {
    return END_ID.equals(this.getId());
  }

  @Override
  public WorkflowNode accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }
}
