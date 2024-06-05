package com.james.playground.temporal.cancellation;

import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = CancellableWorkflow.QUEUE_NAME)
public class CancellableWorkflowImpl implements CancellableWorkflow {
  private static final Logger logger = Workflow.getLogger(CancellableWorkflowImpl.class);

  @Override
  public String waitForCancellation() {
    logger.info("Starting to wait for cancellation");

    SleepyWorkflow sleepyWorkflow = Workflow.newChildWorkflowStub(
        SleepyWorkflow.class,
        ChildWorkflowOptions.newBuilder()
            .setWorkflowId(SleepyWorkflow.WORKFLOW_ID)
            //            .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_TERMINATE) // default
            //            .setCancellationType(ChildWorkflowCancellationType.TRY_CANCEL) // default
            .build()
    );

    return "COMPLETED-" + sleepyWorkflow.sleep();
  }
}
