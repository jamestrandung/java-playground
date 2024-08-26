package com.james.playground.temporal.dsl.activities;

import com.google.protobuf.ByteString;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.spring.boot.ActivityImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = {TemporalActivity.QUEUE_NAME})
public class TemporalActivityImpl implements TemporalActivity {
  @Autowired
  WorkflowServiceStubs workflowServiceStubs;

  @Autowired
  WorkflowClient workflowClient;

  @Override
  public void signalWorkflows(SignalWorkflowsRequest request) {
    ActivityExecutionContext context = Activity.getExecutionContext();

    /*
    At run-time, we may have millions of workflows running. Hence, this activity may
    run for a long time depending on the number of workflow executions we get from
    the given filter. Hence, we need to send heartbeat page by page in case we need
    to retry for any reasons (e.g. redeployment).
     */
    ByteString nextPageToken = context.getHeartbeatDetails(ByteString.class)
        .orElse(ByteString.EMPTY);

    do {
      ListWorkflowExecutionsRequest listRequest = ListWorkflowExecutionsRequest.newBuilder()
          .setNamespace(this.workflowClient.getOptions().getNamespace())
          .setQuery(request.getFilter().toQueryString())
          .setNextPageToken(nextPageToken)
          .setPageSize(LIST_EXECUTION_PAGE_SIZE)
          .build();

      List<CompletableFuture<Void>> futures = new ArrayList<>();

      ListWorkflowExecutionsResponse listResponse = this.workflowServiceStubs.blockingStub().listWorkflowExecutions(listRequest);
      for (WorkflowExecutionInfo executionInfo : listResponse.getExecutionsList()) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
          try {
            WorkflowStub workflowStub = this.workflowClient.newUntypedWorkflowStub(executionInfo.getExecution().getWorkflowId());
            workflowStub.signal(request.getSignalName(), request.getPayload());

          } catch (Exception ex) {
            log.info(
                "Failed to send Signal, workflow ID: {}, error: {}",
                executionInfo.getExecution().getWorkflowId(), ex.getMessage()
            );
          }
        });

        futures.add(future);
      }

      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

      nextPageToken = listResponse.getNextPageToken();
      context.heartbeat(nextPageToken);

    } while (!nextPageToken.isEmpty());
  }
}
