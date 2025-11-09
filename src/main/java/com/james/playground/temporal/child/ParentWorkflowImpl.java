package com.james.playground.temporal.child;

import com.james.playground.temporal.child.DummyActivity.Request;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowCancellationType;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = ParentWorkflow.QUEUE_NAME)
public class ParentWorkflowImpl implements ParentWorkflow {
  private static final RetryOptions RETRY_OPTIONS = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(3)) // Wait duration before first retry
      .setMaximumInterval(Duration.ofSeconds(60)) // Maximum wait duration between retries
      .setMaximumAttempts(3) // Maximum number of retry attempts
      .setBackoffCoefficient(1)
      .build();

  private static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
      // One of the following MUST be set, StartToCloseTimeout is a better option
      .setScheduleToCloseTimeout(Duration.ofSeconds(60)) // Max duration from scheduled to completion, including queue time
      .setStartToCloseTimeout(Duration.ofSeconds(10)) // Max execution time for single Activity
      .setRetryOptions(RETRY_OPTIONS)
      .setTaskQueue(DummyActivity.QUEUE_NAME)
      .build();

  private final DummyActivity activity = Workflow.newActivityStub(DummyActivity.class, ACTIVITY_OPTIONS, null);

  @Override
  public void doSomething() {
    this.activity.consume(Request.builder().texts(List.of("str1")).build());

    ChildWorkflow childWorkflow = Workflow.newChildWorkflowStub(
        ChildWorkflow.class,
        ChildWorkflowOptions.newBuilder()
            .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
            .setCancellationType(ChildWorkflowCancellationType.ABANDON)
            .setTaskQueue(ChildWorkflow.QUEUE_NAME)
            .setWorkflowId(ChildWorkflow.class.getName())
            .build()
    );

    Promise<Void> promise = Async.procedure(childWorkflow::doSomethingElse);

    Workflow.getWorkflowExecution(childWorkflow).get();

    promise.get();

    Workflow.sleep(Duration.ofHours(2));
  }
}
