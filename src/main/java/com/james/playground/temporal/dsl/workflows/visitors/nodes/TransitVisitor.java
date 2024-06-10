package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import io.temporal.workflow.Workflow;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class TransitVisitor extends NodeVisitor<TransitNode> {
  private static final Logger logger = Workflow.getLogger(TransitVisitor.class);

  public TransitVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public WorkflowNode visit(TransitNode node) {
    log.info("TransitNode: {}", node);

    if (node.isEndNode()) {
      return null;
    }

    return this.findNodeIgnoringDeletedNodes(node.getNextNodeId());
  }
}
