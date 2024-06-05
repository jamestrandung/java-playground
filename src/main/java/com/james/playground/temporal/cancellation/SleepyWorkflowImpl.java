package com.james.playground.temporal.cancellation;

import io.temporal.failure.CanceledFailure;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = SleepyWorkflow.QUEUE_NAME)
public class SleepyWorkflowImpl implements SleepyWorkflow {
  private static final Logger logger = Workflow.getLogger(SleepyWorkflowImpl.class);

  @Override
  public String sleep() {
    // Try-catch for cancellation is only required when we
    // need to perform cleanup. Without it, the workflow
    // will still appear as cancelled in the Temporal Web UI.
    try {
      logger.info("Going to sleep for an hour");

      Workflow.sleep(60 * 60 * 1000);

      return "SLEEP_COMPLETED";

    } catch (CanceledFailure ex) {
      logger.info("Cancellation triggered");

      CancellationScope detached = Workflow.newDetachedCancellationScope(() -> {
        logger.info("Performing cleanup upon cancellation");
      });

      detached.run();

      return "SLEEP_CANCELLED";
    }
  }
}
