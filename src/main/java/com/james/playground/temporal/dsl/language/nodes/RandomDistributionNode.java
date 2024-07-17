package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.NodeType.Constants;
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
public class RandomDistributionNode extends BranchingNode {
  @JsonProperty(Constants.PROPERTY_NAME)
  private final NodeType type = NodeType.RANDOM_DISTRIBUTION;

  private List<RandomDistributionBranch> branches;

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
  public static class RandomDistributionBranch {
    private String id;
    private String nextNodeId;
    private Integer probability;
  }
}
