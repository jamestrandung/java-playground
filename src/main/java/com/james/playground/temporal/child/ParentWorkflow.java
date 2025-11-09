package com.james.playground.temporal.child;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ParentWorkflow {
  String QUEUE_NAME = "ParentTaskQueue";

  @WorkflowMethod
  void doSomething();
}
