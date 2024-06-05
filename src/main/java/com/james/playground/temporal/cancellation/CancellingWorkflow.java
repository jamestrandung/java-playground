package com.james.playground.temporal.cancellation;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface CancellingWorkflow {
  String QUEUE_NAME = "CancellingTaskQueue";
  String WORKFLOW_ID = "cancelling-workflow";

  @WorkflowMethod
  String cancel(String workflowIdSuffix);
}
