package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MarketingWorkflow {
  String QUEUE_NAME = "MarketingWorkflowTaskQueue";
  String WORKFLOW_ID_FORMAT = "marketing-workflow-%s-%d";

  @WorkflowMethod
  void execute(DynamicWorkflowInput input);
}
