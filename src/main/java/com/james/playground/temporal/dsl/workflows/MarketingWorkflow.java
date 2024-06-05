package com.james.playground.temporal.dsl.workflows;

import io.temporal.workflow.WorkflowInterface;

@WorkflowInterface
public interface MarketingWorkflow extends DynamicWorkflow {
  String QUEUE_NAME = "MarketingWorkflowTaskQueue";
  String WORKFLOW_ID_FORMAT = "marketing-workflow-%s-%d";
}
