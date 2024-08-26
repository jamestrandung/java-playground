package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.activities.TemporalActivity;
import com.james.playground.temporal.dsl.activities.TemporalActivity.SignalWorkflowsRequest;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import com.james.playground.temporal.utils.AttributeFilter;
import com.james.playground.temporal.utils.WorkflowFilter;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class VersionChangeWorkflowImpl<T extends WorkflowDefinition<T>> implements VersionChangeWorkflow<T> {
  protected static final RetryOptions RETRY_OPTIONS = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(1)) // Wait duration before first retry
      .setMaximumInterval(Duration.ofSeconds(60)) // Maximum wait duration between retries
      .setMaximumAttempts(5) // Maximum number of retry attempts
      .setBackoffCoefficient(2)
      .build();
  protected static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
      // One of the following MUST be set, StartToCloseTimeout is a better option
      //      .setScheduleToCloseTimeout(Duration.ofSeconds(60)) // Max duration from scheduled to completion, including queue time
      .setStartToCloseTimeout(Duration.ofSeconds(240)) // Max execution time for single Activity
      .setRetryOptions(RETRY_OPTIONS)
      .build();

  protected final TemporalActivity temporalActivity = Workflow.newActivityStub(
      TemporalActivity.class,
      ActivityOptions.newBuilder(ACTIVITY_OPTIONS)
          .setHeartbeatTimeout(Duration.ofSeconds(50))
          .setTaskQueue(TemporalActivity.QUEUE_NAME)
          .build(),
      null
  );

  protected List<NodeChangeSignal> detectNodeChanges(Map<String, WorkflowNode> oldNodes, Map<String, WorkflowNode> latestNodes) {
    List<NodeChangeSignal> changes = new ArrayList<>();

    for (Map.Entry<String, WorkflowNode> entry : oldNodes.entrySet()) {
      WorkflowNode old    = entry.getValue();
      WorkflowNode latest = latestNodes.get(entry.getKey());

      old.detectChange(latest)
          .ifPresent(changes::add);
    }

    return changes;
  }

  protected void broadcastNodeChanges(MarketingWorkflowDefinition old, MarketingWorkflowDefinition latest) {
    Map<String, WorkflowNode> oldNodes    = old.getNodes();
    Map<String, WorkflowNode> latestNodes = latest.getNodes();

    List<Promise<Void>> futures = new ArrayList<>();

    for (Map.Entry<String, WorkflowNode> entry : oldNodes.entrySet()) {
      WorkflowNode oldNode    = entry.getValue();
      WorkflowNode latestNode = latestNodes.get(entry.getKey());

      Optional<NodeChangeSignal> signal = oldNode.detectChange(latestNode);
      if (signal.isEmpty()) {
        continue;
      }

      Promise<Void> future = this.broadcastNodeChangesViaSearch(oldNode, signal.get());
      futures.add(future);
    }

    Promise.allOf(futures).get();
  }

  Promise<Void> broadcastNodeChangesViaSearch(WorkflowNode oldNode, NodeChangeSignal signal) {
    AttributeFilter blockingNodeFilter = AttributeFilter.equals(
        SearchAttributeKey.forKeyword("CustomCurrentBlockingNodeId"), oldNode.getId());
    AttributeFilter workflowTypeFilter = AttributeFilter.equals(SearchAttributeKey.forKeyword("WorkflowType"), "MarketingWorkflow");
    AttributeFilter executionStatus    = AttributeFilter.equals(SearchAttributeKey.forKeyword("ExecutionStatus"), "Running");

    WorkflowFilter filter = WorkflowFilter.basic(blockingNodeFilter)
        .and(workflowTypeFilter)
        .and(executionStatus);

    return Async.procedure(
        this.temporalActivity::signalWorkflows,
        SignalWorkflowsRequest.builder()
            .filter(filter)
            .signalName(DynamicWorkflowImpl.CHANGE_SIGNAL_NAME)
            .payload(new Object[]{null, List.of(signal)})
            .build()
    );
  }

  Promise<Void> broadcastNodeChangesViaWorkflow(MarketingWorkflowDefinition old, WorkflowNode oldNode, NodeChangeSignal signal) {
    GroupSignalBroadcastWorkflow workflow = Workflow.newChildWorkflowStub(
        GroupSignalBroadcastWorkflow.class,
        ChildWorkflowOptions.newBuilder()
            .setTaskQueue(GroupSignalBroadcastWorkflow.QUEUE_NAME)
            .setWorkflowId(GroupSignalBroadcastWorkflow.WORKFLOW_ID_PREFIX + old.getWorkflowIdPrefix() + oldNode.getType())
            .build()
    );

    return Async.procedure(
        workflow::broadcastSignalToGroup,
        GroupSignalInput.builder()
            .groupId(signal.getTargetGroupId())
            .workflowIdPrefix(old.getWorkflowIdPrefix())
            .signalName(DynamicWorkflowImpl.CHANGE_SIGNAL_NAME)
            .payload(new Object[]{null, List.of(signal)})
            .pageIdx(0)
            .pageSize(10000)
            .build()
    );
  }
}
