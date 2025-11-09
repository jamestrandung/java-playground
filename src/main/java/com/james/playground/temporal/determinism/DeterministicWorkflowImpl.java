package com.james.playground.temporal.determinism;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WorkflowImpl(taskQueues = DeterministicWorkflow.QUEUE_NAME)
public class DeterministicWorkflowImpl implements DeterministicWorkflow {
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
      .setTaskQueue(PrintActivity.QUEUE_NAME)
      .build();

  private final PrintActivity printActivity = Workflow.newActivityStub(PrintActivity.class, ACTIVITY_OPTIONS, null);

  @Override
  public String execute() {
    this.testConcurrency();
    return "";
  }

  void testConcurrency() {
    List<Promise<Void>> promises = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      Promise<Void> promise = Async.procedure(this::randomPrint);
      promises.add(promise);
    }

    Promise.allOf(promises).get();

    this.printActivity.echo(List.of("abc", "def"));
  }

  void randomPrint() {
    String text = "abcdefghijklmnopqrstuvwxyz";

    Set<String> strs = new HashSet<>();
    for (int i = 0; i < 1; i++) {
      int idx = new Random().nextInt(26);
      strs.add(String.valueOf(text.charAt(idx)));
    }

    this.printActivity.echo(new ArrayList<>(strs));
    this.printActivity.print(new ArrayList<>(strs));
  }

  void testArgumentLength() {
    String text = "abcdefghijklmnopqrstuvwxyz";

    Set<String> strs = new HashSet<>();
    for (int i = 0; i < 4; i++) {
      int idx = new Random().nextInt(26);
      strs.add(String.valueOf(text.charAt(idx)));
    }

    if (strs.size() <= 3) {
      this.printActivity.echo(new ArrayList<>(strs));
    }

    //    this.printActivity.echo(new ArrayList<>(strs));

    strs = new HashSet<>();
    for (int i = 0; i < 4; i++) {
      int idx = new Random().nextInt(26);
      strs.add(String.valueOf(text.charAt(idx)));
    }

    this.printActivity.print(new ArrayList<>(strs));
  }
}
