package com.james.playground.temporal.signal;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SignalReceiverWorkflow {
  String QUEUE_NAME = "SignalTaskQueue";
  String WORKFLOW_ID_PREFIX = "signal-receiver-workflow-";

  @WorkflowMethod
  String waitForSignal();

  @SignalMethod
  void unblock(String message);
}
