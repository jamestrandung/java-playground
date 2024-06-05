package com.james.playground.temporal.cancellation;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = CancellingWorkflow.QUEUE_NAME)
public class CancellingWorkflowImpl implements CancellingWorkflow {
  private static final Logger logger = Workflow.getLogger(CancellingWorkflowImpl.class);

  @Override
  public String cancel(String workflowIdSuffix) {
    logger.info("Starting to cancel sleeping workflow");

    try {
      // If WorkflowId exists, the request will succeed regardless of
      // whether the workflow is running or not. On the other hand,
      // if WorkflowId does not exist, we will get an error.
      //
      // We MUST wrap this call inside a try-catch. Otherwise, this
      // workflow will be stuck in a retry loop and keep sending
      // cancellation requests. Even if we spawn a workflow with
      // the cancelled workflowId, this workflow will not be able
      // to reach it.
      Workflow.newUntypedExternalWorkflowStub(CancellableWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
          .cancel();

    } catch (Exception ex) {
      logger.error("Failed to cancel sleepy workflow", ex);
      return "CANCELLATION_FAILED";
    }

    return "CANCELLATION_COMPLETED";
  }
}
