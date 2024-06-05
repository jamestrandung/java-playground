package com.james.playground.temporal.signal;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = SignalReceiverWorkflow.QUEUE_NAME)
public class SignalReceiverWorkflowImpl implements SignalReceiverWorkflow {
  private static final Logger logger = Workflow.getLogger(SignalReceiverWorkflowImpl.class);

  String message = null;

  @Override
  public String waitForSignal() {
    logger.info("Starting to wait for Signal");

    Workflow.await(() -> StringUtils.isNotBlank(message));

    logger.info("Received Signal");

    return "COMPLETED-" + message;
  }

  @Override
  public void unblock(String message) {
    this.message = message;
  }
}
