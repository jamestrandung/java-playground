package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import io.temporal.workflow.WorkflowMethod;

public interface VersionChangeWorkflow<T extends WorkflowDefinition<T>> {
  @WorkflowMethod
  void detectChangesAndBroadcastSignals(T old, T latest);
}
