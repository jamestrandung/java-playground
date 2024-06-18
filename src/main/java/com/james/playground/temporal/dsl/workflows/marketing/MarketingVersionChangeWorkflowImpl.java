package com.james.playground.temporal.dsl.workflows.marketing;

import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflowImpl;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import com.james.playground.temporal.dsl.workflows.core.VersionChangeWorkflowImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import java.util.List;
import java.util.Optional;

@WorkflowImpl(taskQueues = MarketingVersionChangeWorkflow.QUEUE_NAME)
public class MarketingVersionChangeWorkflowImpl
    extends VersionChangeWorkflowImpl<MarketingWorkflowDefinition>
    implements MarketingVersionChangeWorkflow {
  @Override
  public void detectChangesAndBroadcastSignals(MarketingWorkflowDefinition old, MarketingWorkflowDefinition latest) {
    Optional<WorkflowChangeSignal> workflowChangeSignal = old.detectChange(latest);
    if (workflowChangeSignal.isEmpty()) {
      this.broadcastNodeChanges(old, latest);
      return;
    }

    List<NodeChangeSignal> nodeChangeSignals = this.detectNodeChanges(old.getNodes(), latest.getNodes());

    GroupSignalBroadcastWorkflow workflow = Workflow.newChildWorkflowStub(
        GroupSignalBroadcastWorkflow.class,
        ChildWorkflowOptions.newBuilder()
            .setTaskQueue(GroupSignalBroadcastWorkflow.QUEUE_NAME)
            .setWorkflowId(GroupSignalBroadcastWorkflow.WORKFLOW_ID_PREFIX + old.getWorkflowIdPrefix())
            .build()
    );

    workflow.broadcastSignalToGroup(
        GroupSignalInput.builder()
            .groupId(old.getContext().getActiveGroupId())
            .workflowIdPrefix(old.getWorkflowIdPrefix())
            .signalName(DynamicWorkflowImpl.CHANGE_SIGNAL_NAME)
            .payload(new Object[]{List.of(workflowChangeSignal.get()), nodeChangeSignals})
            .pageIdx(0)
            .pageSize(10000)
            .build()
    );
  }
}
