package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.PrinterActivity;
import com.james.playground.temporal.dsl.activities.UserActivity;
import com.james.playground.temporal.dsl.activities.UserGroupActivity;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.LocalActivityOptions;
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

  protected final UserActivity userActivity = Workflow.newActivityStub(
      UserActivity.class,
      ActivityOptions.newBuilder(ACTIVITY_OPTIONS)
          .setTaskQueue(UserActivity.QUEUE_NAME)
          .build(),
      null
  );

  protected final UserGroupActivity localUserGroupActivity = Workflow.newLocalActivityStub(
      UserGroupActivity.class,
      LocalActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .build(),
      null
  );

  protected final UserActivity localUserActivity = Workflow.newLocalActivityStub(
      UserActivity.class,
      LocalActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(5))
          .build(),
      null
  );

  protected DynamicWorkflowInput input;

  public abstract String visit(T node);
}
