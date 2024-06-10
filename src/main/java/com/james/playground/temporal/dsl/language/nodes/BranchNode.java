package com.james.playground.temporal.dsl.language.nodes;

import com.james.playground.temporal.dsl.language.Condition;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.util.List;
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
public class BranchNode extends WorkflowNode {
  private List<Condition> conditions;

  @Override
  public WorkflowNode accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }
}
