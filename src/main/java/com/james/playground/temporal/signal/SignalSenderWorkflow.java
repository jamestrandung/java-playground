package com.james.playground.temporal.signal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SignalSenderWorkflow {
  String QUEUE_NAME = "SignalTaskQueue";
  String WORKFLOW_ID = "signal-sender-workflow";

  @WorkflowMethod
  String sendSignal(String workflowIdSuffix, String message);
}
