package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class DynamicWorkflowImpl implements DynamicWorkflow {
  private static final Logger logger = Workflow.getLogger(DynamicWorkflowImpl.class);

  private DynamicWorkflowInput input;
  private DelegatingVisitor visitor;

  @Override
  public void execute(DynamicWorkflowInput input) {
    this.init(input);

    WorkflowNode node = this.visitor.findNodeIgnoringDeletedNodes(TransitNode.START_ID);
    while (node != null) {
      log.info("Node: {}", node);
      node = node.accept(this.visitor);
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
}
