package com.james.playground.controller.temporal;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.james.playground.temporal.cancellation.CancellableWorkflow;
import com.james.playground.temporal.cancellation.CancellingWorkflow;
import com.james.playground.temporal.utils.AttributeFilter;
import com.james.playground.temporal.utils.WorkflowFilter;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.api.workflowservice.v1.RequestCancelWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.serviceclient.WorkflowServiceStubs;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

  @Autowired
  private WorkflowServiceStubs workflowServiceStubs;

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
  public void cancelViaSearch(@RequestParam Long userId) throws ExecutionException, InterruptedException {
    AttributeFilter userFilter = AttributeFilter.equals(SearchAttributeKey.forLong("CustomUserId"), userId);
    AttributeFilter workflowTypeFilter = AttributeFilter.equals(SearchAttributeKey.forKeyword("WorkflowType"), "MarketingWorkflow");
    AttributeFilter executionStatus = AttributeFilter.equals(SearchAttributeKey.forKeyword("ExecutionStatus"), "Running");

    WorkflowFilter filter = WorkflowFilter.basic(userFilter)
        .and(workflowTypeFilter)
        .and(executionStatus);

    log.info("Filter: {}", filter.toString());

    ByteString nextPageToken = ByteString.EMPTY;

    do {
      ListWorkflowExecutionsRequest request = ListWorkflowExecutionsRequest.newBuilder()
          .setNamespace(this.workflowClient.getOptions().getNamespace())
          .setQuery(filter.toString())
          .setNextPageToken(nextPageToken)
          .setPageSize(500)
          .build();

      List<ListenableFuture<?>> futures = new ArrayList<>();

      ListWorkflowExecutionsResponse response = this.workflowServiceStubs.blockingStub().listWorkflowExecutions(request);
      for (WorkflowExecutionInfo executionInfo : response.getExecutionsList()) {
        try {
          RequestCancelWorkflowExecutionRequest req = RequestCancelWorkflowExecutionRequest.newBuilder()
              .setRequestId(UUID.randomUUID().toString())
              .setWorkflowExecution(executionInfo.getExecution())
              .setNamespace(this.workflowClient.getOptions().getNamespace())
              .setIdentity(this.workflowClient.getOptions().getIdentity())
              .setReason("Because I like")
              .build();

          var future = this.workflowServiceStubs.futureStub().requestCancelWorkflowExecution(req);

          futures.add(
              Futures.catching(future, Exception.class, ex -> {
                log.error(
                    "TemporalActivityImpl.cancelWorkflows failed, workflow ID: {}, error: {}",
                    executionInfo.getExecution().getWorkflowId(), ExceptionUtils.getStackTrace(ex)
                );

                return null;
              }, MoreExecutors.directExecutor())
          );

        } catch (Exception ex) {
          log.error("Cancellation failure: {}", ex.getMessage());
        }
      }

      Futures.allAsList(futures).get();

      nextPageToken = response.getNextPageToken();

    } while (!nextPageToken.isEmpty());
  }
}
