package com.james.playground.temporal.dsl.language.nodes;

import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
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
public class RandomDistributionNode extends WorkflowNode {
  private List<String> nextNodeIds;

  @Override
  public String accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public String getType() {
    return NodeType.RANDOM_DISTRIBUTION;
  }
}
