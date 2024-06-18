package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowMethod;
import java.util.List;

public interface DynamicWorkflow<T extends WorkflowChangeSignal> {
  @WorkflowMethod
  void execute(DynamicWorkflowInput input);

  @SignalMethod
  void handleChangeSignals(List<T> workflowSignals, List<NodeChangeSignal> nodeSignals);
}
