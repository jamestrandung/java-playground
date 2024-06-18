package com.james.playground.temporal.dsl.workflows.marketing;

import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflow;
import io.temporal.workflow.WorkflowInterface;

@WorkflowInterface
public interface MarketingWorkflow extends DynamicWorkflow<MarketingWorkflowChangeSignal> {
  String QUEUE_NAME = "MarketingWorkflowTaskQueue";
  String WORKFLOW_ID_PREFIX = "marketing-workflow-%s-";
  String WORKFLOW_ID_FORMAT = WORKFLOW_ID_PREFIX + "%d";
}
