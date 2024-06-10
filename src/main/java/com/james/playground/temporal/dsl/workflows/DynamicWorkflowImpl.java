package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.activities.PrinterActivity.PrinterInput;
import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.DelayNode;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicWorkflowImpl extends DynamicWorkflowConfigs {
  //  private static final Logger logger = Workflow.getLogger(DynamicWorkflowImpl.class);

  private DynamicWorkflowInput input;

  public void execute(DynamicWorkflowInput input) {
    this.input = input;

    WorkflowNode node = this.findNode(TransitNode.START_ID);
    while (node != null) {
      log.info("Node: {}", node);
      node = node.accept(this);
    }
  }

  public WorkflowNode visit(TransitNode node) {
    log.info("TransitNode: {}", node);

    if (node.isEndNode()) {
      return null;
    }

    return this.findNode(node.getNextNodeId());
  }

  public WorkflowNode visit(DelayNode node) {
    log.info("DelayNode: {}", node);
    log.info("Sleeping for {} seconds", node.getDurationInSeconds());

    Duration duration = node.getDuration();
    if (!duration.isZero()) {
      Workflow.sleep(duration);
    }

    return this.findNode(node.getNextNodeId());
  }

  public WorkflowNode visit(PrinterNode node) {
    log.info("PrinterNode: {}", node);

    DynamicActivityResult result = this.printerActivity.print(
        PrinterInput.builder()
            .node(node)
            .userId(this.input.getUserId())
            .build()
    );

    return this.findNode(result.getNextNodeId());
  }

  WorkflowNode findNode(String nodeId) {
    return Workflow.sideEffect(
        WorkflowNode.class,
        () -> WorkflowStore.INSTANCE.find(this.input.getWorkflowDefinitionId(), nodeId)
    );
  }
}
