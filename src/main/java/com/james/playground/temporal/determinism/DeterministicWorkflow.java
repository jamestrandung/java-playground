package com.james.playground.temporal.determinism;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DeterministicWorkflow {
  String QUEUE_NAME = "DeterministicTaskQueue";

  @WorkflowMethod
  String execute();
}
