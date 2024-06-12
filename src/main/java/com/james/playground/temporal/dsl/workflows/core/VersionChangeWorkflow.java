package com.james.playground.temporal.dsl.workflows.core;

//@WorkflowInterface
public interface VersionChangeWorkflow {
  String QUEUE_NAME = "VersionChangeWorkflowTaskQueue";
  String WORKFLOW_ID_FORMAT = "version-change-workflow-%s-%d";
}
