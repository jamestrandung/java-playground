package com.james.playground.controller.temporal;

import com.james.playground.temporal.cancellation.CancellableWorkflow;
import com.james.playground.temporal.cancellation.CancellingWorkflow;
import com.james.playground.temporal.utils.AttributeFilter;
import com.james.playground.temporal.utils.WorkflowFilter;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal/cancellation")
public class CancellationController {
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping
  public void waitForCancellation(@RequestParam String workflowIdSuffix) {
    CancellableWorkflow workflow = this.workflowClient.newWorkflowStub(
        CancellableWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(CancellableWorkflow.QUEUE_NAME)
            .setWorkflowId(CancellableWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
            .build()
    );

    WorkflowClient.execute(workflow::waitForCancellation)
        .thenAccept(result -> {
          log.info("End result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("End failure: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/direct")
  public void cancelDirectly(@RequestParam String workflowIdSuffix) {
    // If WorkflowId exists, the request will succeed regardless of
    // whether the workflow is running or not. On the other hand,
    // if WorkflowId does not exist, we will get an error.
    this.workflowClient.newUntypedWorkflowStub(CancellableWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
        .cancel();

    log.info("Cancellation completed");
  }

  @PostMapping("/workflow")
  public void cancelViaWorkflow(@RequestParam String workflowIdSuffix) {
    CancellingWorkflow workflow = this.workflowClient.newWorkflowStub(
        CancellingWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(CancellingWorkflow.QUEUE_NAME)
            .setWorkflowId(CancellingWorkflow.WORKFLOW_ID)
            .build()
    );

    WorkflowClient.execute(workflow::cancel, workflowIdSuffix)
        .thenAccept(result -> {
          log.info("Cancellation result: {}", result);
        })
        .exceptionally(ex -> {
          log.error("Cancellation failure: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/search")
  public void cancelViaSearch(@RequestParam Long userId) {
    AttributeFilter userFilter         = AttributeFilter.create("CustomUserId", userId);
    AttributeFilter workflowTypeFilter = AttributeFilter.create("WorkflowType", "MarketingWorkflow");
    AttributeFilter executionStatus    = AttributeFilter.create("ExecutionStatus", "Running");

    WorkflowFilter filter = WorkflowFilter.basic(userFilter)
        .and(workflowTypeFilter)
        .and(executionStatus);

    log.info("Filter: {}", filter.toString());

    this.workflowClient.listExecutions(filter.toString())
        .forEach(metadata -> {
          try {
            this.workflowClient.newUntypedWorkflowStub(metadata.getExecution().getWorkflowId())
                .cancel();
            log.info("Cancelled: {}", metadata.getExecution().getWorkflowId());
          } catch (Exception ex) {
            log.error("Cancellation failure: {}", ex.getMessage());
          }
        });
  }
}
