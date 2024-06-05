package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.activities.PrinterActivity.PrinterInput;
import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.DelayNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionType;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicWorkflowImpl extends DynamicWorkflowConfigs implements DynamicWorkflow {
  //  private static final Logger logger = Workflow.getLogger(DynamicWorkflowImpl.class);

  private DynamicWorkflowInput input;

  // Delay markers
  private long delayStartTimestamp;
  private DelayInterruptionType delayInterruptionType;

  @Override
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

    Duration nodeDuration      = node.getDuration();
    Duration remainingDuration = nodeDuration;

    // Upon revisiting the node due to interruption,
    // we need to calculate the remaining duration
    if (this.delayInterruptionType != null) {
      long     now             = Workflow.currentTimeMillis();
      Duration elapsedDuration = Duration.between(Instant.ofEpochMilli(this.delayStartTimestamp), Instant.ofEpochMilli(now));

      log.info("Already slept for {} seconds", elapsedDuration.toSeconds());
      remainingDuration = nodeDuration.minus(elapsedDuration);
    }

    if (remainingDuration.isZero() || remainingDuration.isNegative()) {
      this.resetDelayMarkers();
      return this.findNode(node.getNextNodeId());
    }

    // Capture the start timestamp only on the very first visit of the node
    this.delayStartTimestamp = this.delayStartTimestamp == 0 ? Workflow.currentTimeMillis() : this.delayStartTimestamp;
    this.delayInterruptionType = null;

    log.info("Sleeping for {} seconds", remainingDuration.toSeconds());

    Workflow.await(remainingDuration, () -> this.delayInterruptionType != null);

    // If the delay duration was modified, we need to re-visit the current node
    if (this.delayInterruptionType == DelayInterruptionType.DURATION_MODIFIED) {
      WorkflowNode latestNode = this.findNode(node.getId());
      return latestNode.accept(this);
    }

    this.resetDelayMarkers();
    return this.findNode(node.getNextNodeId());
  }

  @Override
  public void interruptDelay(DelayInterruptionType type) {
    if (this.delayStartTimestamp == 0) {
      // Ignore the Signal as no delay is ongoing
      return;
    }

    this.delayInterruptionType = type;
  }

  void resetDelayMarkers() {
    this.delayStartTimestamp = 0;
    this.delayInterruptionType = null;
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
