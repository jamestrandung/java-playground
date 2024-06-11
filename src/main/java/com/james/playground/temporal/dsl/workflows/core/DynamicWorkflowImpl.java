package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;

@Slf4j
public class DynamicWorkflowImpl implements DynamicWorkflow {
  private static final Logger logger = Workflow.getLogger(DynamicWorkflowImpl.class);

  protected DynamicWorkflowInput input;
  protected DelegatingVisitor visitor;

  @Override
  public void execute(DynamicWorkflowInput input) {
    this.init(input);

    String nextNodeId = TransitNode.START_ID;
    while (StringUtils.isNotBlank(nextNodeId)) {
      WorkflowNode node = this.findNodeIgnoringDeletedNodes(nextNodeId);
      log.info("Node: {}", node);

      nextNodeId = node.accept(this.visitor);
    }
  }

  void init(DynamicWorkflowInput input) {
    this.input = input;
    this.visitor = new DelegatingVisitor(input);
  }

  @Override
  public void interruptDelay(DelayInterruptionSignal signal) {
    this.visitor.interruptDelay(signal);
  }

  WorkflowNode findNodeIgnoringDeletedNodes(String nodeId) {
    return Workflow.sideEffect(
        WorkflowNode.class,
        () -> WorkflowStore.getInstance().findNodeIgnoringDeletedNodes(this.input.getWorkflowDefinitionId(), nodeId)
    );
  }
}
