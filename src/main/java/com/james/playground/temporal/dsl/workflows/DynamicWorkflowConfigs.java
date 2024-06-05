package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.activities.PrinterActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class DynamicWorkflowConfigs {
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
}
