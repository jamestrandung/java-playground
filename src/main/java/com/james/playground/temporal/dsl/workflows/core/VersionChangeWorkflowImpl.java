package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class VersionChangeWorkflowImpl<T extends WorkflowDefinition<T>> implements VersionChangeWorkflow<T> {
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

      GroupSignalBroadcastWorkflow workflow = Workflow.newChildWorkflowStub(
          GroupSignalBroadcastWorkflow.class,
          ChildWorkflowOptions.newBuilder()
              .setTaskQueue(GroupSignalBroadcastWorkflow.QUEUE_NAME)
              .setWorkflowId(GroupSignalBroadcastWorkflow.WORKFLOW_ID_PREFIX + old.getWorkflowIdPrefix() + oldNode.getType())
              .build()
      );

      Promise<Void> future = Async.procedure(
          workflow::broadcastSignalToGroup,
          GroupSignalInput.builder()
              .groupId(signal.get().getTargetGroupId())
              .workflowIdPrefix(old.getWorkflowIdPrefix())
              .signalName(DynamicWorkflowImpl.CHANGE_SIGNAL_NAME)
              .payload(new Object[]{null, List.of(signal.get())})
              .pageIdx(0)
              .pageSize(10000)
              .build()
      );

      futures.add(future);
    }

    Promise.allOf(futures).get();
  }
}
