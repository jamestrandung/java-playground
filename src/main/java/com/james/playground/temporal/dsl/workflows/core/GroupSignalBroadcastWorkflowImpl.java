package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.activities.UserGroupActivity;
import com.james.playground.temporal.dsl.activities.UserGroupActivity.GetUserIdsInGroupInput;
import com.james.playground.temporal.dsl.activities.UserGroupActivity.GetUserIdsInGroupOutput;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.ExternalWorkflowStub;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@WorkflowImpl(taskQueues = GroupSignalBroadcastWorkflow.QUEUE_NAME)
public class GroupSignalBroadcastWorkflowImpl implements GroupSignalBroadcastWorkflow {
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
  // There's a hard limit of 2000 pending signal at any given points
  private static final int SIGNAL_BATCH_SIZE = 2000;
  private static final Logger LOGGER = Workflow.getLogger(GroupSignalBroadcastWorkflowImpl.class);
  protected final UserGroupActivity userGroupActivity = Workflow.newActivityStub(
      UserGroupActivity.class,
      ActivityOptions.newBuilder(ACTIVITY_OPTIONS)
          .setHeartbeatTimeout(Duration.ofSeconds(30))
          .setTaskQueue(UserGroupActivity.QUEUE_NAME)
          .build(),
      null
  );

  @Override
  public <T> void broadcastSignalToGroup(GroupSignalInput<T> input) {
    this.broadcastInWorkflow(input);
    //    this.broadcastInActivity(input);
  }

  <T> void broadcastInActivity(GroupSignalInput<T> input) {
    LOGGER.info("Started broadcasting Signal from Activity, input: {}", input);

    this.userGroupActivity.broadcastSignalToGroup(input);

    LOGGER.info("Completed broadcasting Signal from Activity");
  }

  <T> void broadcastInWorkflow(GroupSignalInput<T> input) {
    LOGGER.info("Started broadcasting Signal from Workflow, input: {}", input);

    GetUserIdsInGroupOutput output = this.userGroupActivity.getUserIdsInGroup(
        GetUserIdsInGroupInput.builder()
            .groupId(input.getGroupId())
            .partitionId(input.getGroupId())
            .pageIdx(input.getPageIdx())
            .pageSize(input.getPageSize())
            .build()
    );

    List<Long> userIds = output.getUserIds();

    int startIdx = 0;
    int endIdx   = SIGNAL_BATCH_SIZE;

    while (startIdx < userIds.size()) {
      LOGGER.info("Working on index range: {} - {}", startIdx, endIdx);

      List<Promise<Void>> futures = new ArrayList<>();

      for (int i = startIdx; i < endIdx && i < userIds.size(); i++) {
        Long userId = userIds.get(i);

        ExternalWorkflowStub receiver = Workflow.newUntypedExternalWorkflowStub(input.getWorkflowIdPrefix() + userId);

        // https://community.temporal.io/t/workflowrejectedexecutionerror-whats-the-cause-and-how-to-solve/871/2?u=jamestran
        Promise<Void> future = Async.procedure(receiver::signal, input.getSignalName(), input.getPayload())
            .exceptionally(ex -> {
              LOGGER.info(
                  "Failed to send Signal, user ID: {}, workflow ID: {}, error: {}",
                  userId, input.getWorkflowIdPrefix() + userId, ex.getMessage()
              );

              return null;
            });

        futures.add(future);
      }

      Promise.allOf(futures).get();

      startIdx = endIdx;
      endIdx += SIGNAL_BATCH_SIZE;
    }

    if (!output.isHasNext()) {
      LOGGER.info("Completed broadcasting Signal from Workflow");
      return;
    }

    Workflow.continueAsNew(
        GroupSignalInput.builder()
            .groupId(input.getGroupId())
            .workflowIdPrefix(input.getWorkflowIdPrefix())
            .signalName(input.getSignalName())
            .payload(input.getPayload())
            .pageIdx(input.getPageIdx() + 1)
            .pageSize(input.getPageSize())
            .build()
    );

    // Does not get printed
    LOGGER.info("TEST TO SEE IF THIS LINE IS EXECUTED");
  }
}
