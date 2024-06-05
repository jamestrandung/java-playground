package com.james.playground.temporal.cancellation;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CancellableWorkflow {
  String QUEUE_NAME = "CancellableTaskQueue";
  String WORKFLOW_ID_PREFIX = "cancellable-workflow-";

  @WorkflowMethod
  String waitForCancellation();
}
