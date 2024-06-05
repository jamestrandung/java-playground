package com.james.playground.temporal.signal;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ExternalWorkflowStub;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = SignalSenderWorkflow.QUEUE_NAME)
public class SignalSenderWorkflowImpl implements SignalSenderWorkflow {
  private static final Logger logger = Workflow.getLogger(SignalSenderWorkflowImpl.class);

  @Override
  public String sendSignal(String workflowIdSuffix, String message) {
    logger.info("Starting to send Signal from Workflow");

    // If WorkflowId does not exist or the workflow is not running,
    // we will get an error. However, compared to cancellation, the
    // workflow will fail immediately on this error instead of being
    // stuck in a retry loop.
    ExternalWorkflowStub receiver = Workflow.newUntypedExternalWorkflowStub(
        SignalReceiverWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix);

    receiver.signal("unblock", message);

    return "COMPLETED_SIGNAL";
  }
}
