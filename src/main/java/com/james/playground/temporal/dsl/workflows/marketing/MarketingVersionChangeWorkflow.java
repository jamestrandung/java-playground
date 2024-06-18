package com.james.playground.temporal.dsl.workflows.marketing;

import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.workflows.core.VersionChangeWorkflow;
import io.temporal.workflow.WorkflowInterface;

@WorkflowInterface
public interface MarketingVersionChangeWorkflow extends VersionChangeWorkflow<MarketingWorkflowDefinition> {
  String QUEUE_NAME = "MarketingVersionChangeWorkflowTaskQueue";
  String WORKFLOW_ID_PREFIX = "marketing-version-change-workflow-";
}
