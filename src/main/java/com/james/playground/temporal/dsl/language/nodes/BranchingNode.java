package com.james.playground.temporal.dsl.language.nodes;

import com.james.playground.temporal.dsl.language.core.WorkflowNode;
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
public abstract class BranchingNode extends WorkflowNode {
  private String convergenceNodeId;
}
