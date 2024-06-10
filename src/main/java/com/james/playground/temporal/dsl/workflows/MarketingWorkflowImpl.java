package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflowImpl;
import io.temporal.spring.boot.WorkflowImpl;

@WorkflowImpl(taskQueues = MarketingWorkflow.QUEUE_NAME)
public class MarketingWorkflowImpl extends DynamicWorkflowImpl implements MarketingWorkflow {

}
