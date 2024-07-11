package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.Condition;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwitchNode extends WorkflowNode {
  @JsonProperty(NodeType.PROPERTY_NAME)
  private final String type = NodeType.SWITCH;

  private String convergenceNodeId;
  private List<SwitchCase> cases;

  @Override
  public String accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public Optional<NodeChangeSignal> detectChange(WorkflowNode latest) {
    return Optional.empty();
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SwitchCase {
    private String name;
    private Condition condition;
    private String nextNodeId;
  }
}
