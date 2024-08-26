package com.james.playground.temporal.dsl.workflows.marketing;

import com.james.playground.temporal.dsl.activities.TemporalActivity.SignalWorkflowsRequest;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflowImpl;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow;
import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow.GroupSignalInput;
import com.james.playground.temporal.dsl.workflows.core.VersionChangeWorkflowImpl;
import com.james.playground.temporal.utils.AttributeFilter;
import com.james.playground.temporal.utils.WorkflowFilter;
import io.temporal.common.SearchAttributeKey;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    this.broadcastNodeChangesViaSearch(old, workflowChangeSignal.get(), nodeChangeSignals);
  }

  void broadcastNodeChangesViaSearch(
      MarketingWorkflowDefinition old, WorkflowChangeSignal workflowChangeSignal, List<NodeChangeSignal> nodeChangeSignals
  ) {
    AttributeFilter workflowTypeFilter    = AttributeFilter.equals(SearchAttributeKey.forKeyword("WorkflowType"), "MarketingWorkflow");
    AttributeFilter executionStatusFilter = AttributeFilter.equals(SearchAttributeKey.forKeyword("ExecutionStatus"), "Running");
    AttributeFilter workflowIdFilter = AttributeFilter.between(
        SearchAttributeKey.forKeyword("WorkflowId"), "marketing-workflow-", "marketing-workflow-" + old.getId() + "-~");

    WorkflowFilter filter = WorkflowFilter.basic(workflowTypeFilter)
        .and(executionStatusFilter)
        .and(workflowIdFilter);

    log.info(filter.toQueryString());

    this.temporalActivity.signalWorkflows(
        SignalWorkflowsRequest.builder()
            .filter(filter)
            .signalName(DynamicWorkflowImpl.CHANGE_SIGNAL_NAME)
            .payload(new Object[]{List.of(workflowChangeSignal), nodeChangeSignals})
            .build()
    );
  }

  void broadcastNodeChangesViaWorkflow(
      MarketingWorkflowDefinition old, WorkflowChangeSignal workflowChangeSignal, List<NodeChangeSignal> nodeChangeSignals
  ) {
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
            .payload(new Object[]{List.of(workflowChangeSignal), nodeChangeSignals})
            .pageIdx(0)
            .pageSize(10000)
            .build()
    );
  }
}
