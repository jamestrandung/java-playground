package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowMethod;

public interface DynamicWorkflow {
  @WorkflowMethod
  void execute(DynamicWorkflowInput input);

  @SignalMethod
  void interruptDelay(DelayInterruptionSignal signal);
}
