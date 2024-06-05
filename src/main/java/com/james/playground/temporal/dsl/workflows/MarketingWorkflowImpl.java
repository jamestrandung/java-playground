package com.james.playground.temporal.dsl.workflows;

import io.temporal.spring.boot.WorkflowImpl;

@WorkflowImpl(taskQueues = MarketingWorkflow.QUEUE_NAME)
public class MarketingWorkflowImpl extends DynamicWorkflowImpl implements MarketingWorkflow {

}
