package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.NodeType.Constants;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.util.Optional;
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

  @JsonProperty(Constants.PROPERTY_NAME)
  private final NodeType type = NodeType.TRANSIT;

  private TransitCategory category;

  @JsonIgnore
  public boolean isEndNode() {
    return END_ID.equals(this.getId());
  }

  @Override
  public String accept(DelegatingVisitor visitor) {
    return null;
  }

  @Override
  public Optional<NodeChangeSignal> detectChange(WorkflowNode latest) {
    return Optional.empty();
  }

  @Override
  protected boolean canBeDeleted() {
    return this.category != TransitCategory.ENTRY_EXIT && this.category != TransitCategory.CONVERGENCE;
  }

  public enum TransitCategory {
    ENTRY_EXIT,
    GO_TO_ACTION,
    CONVERGENCE
  }
}
