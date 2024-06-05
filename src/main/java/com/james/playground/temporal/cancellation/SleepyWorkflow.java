package com.james.playground.temporal.cancellation;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SleepyWorkflow {
  String QUEUE_NAME = "CancellableTaskQueue";
  String WORKFLOW_ID = "sleepy-workflow";

  @WorkflowMethod
  String sleep();
}
