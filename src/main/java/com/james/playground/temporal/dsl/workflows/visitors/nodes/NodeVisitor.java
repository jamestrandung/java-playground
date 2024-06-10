package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.PrinterActivity;
import com.james.playground.temporal.dsl.activities.UserGroupActivity;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class NodeVisitor<T extends WorkflowNode> {
  protected static final RetryOptions RETRY_OPTIONS = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(1)) // Wait duration before first retry
      .setMaximumInterval(Duration.ofSeconds(60)) // Maximum wait duration between retries
      .setMaximumAttempts(3) // Maximum number of retry attempts
      .setBackoffCoefficient(1)
      .build();

  protected static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
      // One of the following MUST be set, StartToCloseTimeout is a better option
      .setScheduleToCloseTimeout(Duration.ofSeconds(60)) // Max duration from scheduled to completion, including queue time
      .setStartToCloseTimeout(Duration.ofSeconds(10)) // Max execution time for single Activity
      .setRetryOptions(RETRY_OPTIONS)
      .build();

  protected final PrinterActivity printerActivity = Workflow.newActivityStub(
      PrinterActivity.class,
      ActivityOptions.newBuilder(ACTIVITY_OPTIONS)
          .setTaskQueue(PrinterActivity.QUEUE_NAME)
          .build(),
      null
  );

  protected final UserGroupActivity userGroupActivity = Workflow.newActivityStub(
      UserGroupActivity.class,
      ActivityOptions.newBuilder(ACTIVITY_OPTIONS)
          .setTaskQueue(UserGroupActivity.QUEUE_NAME)
          .build(),
      null
  );

  protected DynamicWorkflowInput input;

  public abstract WorkflowNode visit(T node);

  public WorkflowNode findNodeIgnoringDeletedNodes(String nodeId) {
    return Workflow.sideEffect(
        WorkflowNode.class,
        () -> WorkflowStore.getInstance().findNodeIgnoringDeletedNodes(this.input.getWorkflowDefinitionId(), nodeId)
    );
  }

  public WorkflowNode findNodeAcceptingDeletedNode(String nodeId) {
    return Workflow.sideEffect(
        WorkflowNode.class,
        () -> WorkflowStore.getInstance().findNodeAcceptingDeletedNode(this.input.getWorkflowDefinitionId(), nodeId)
    );
  }
}
