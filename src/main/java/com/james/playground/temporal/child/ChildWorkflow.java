package com.james.playground.temporal.child;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ChildWorkflow {
  String QUEUE_NAME = "ChildTaskQueue";

  @WorkflowMethod
  void doSomethingElse();
}
