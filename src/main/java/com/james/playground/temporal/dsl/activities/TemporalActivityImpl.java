package com.james.playground.temporal.dsl.activities;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.api.common.v1.Payloads;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.api.workflowservice.v1.SignalWorkflowExecutionRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.interceptors.Header;
import io.temporal.internal.common.HeaderUtils;
import io.temporal.payload.context.WorkflowSerializationContext;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.spring.boot.ActivityImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
    try {
      this.doSignalWorkflows(request);
    } catch (Exception ex) {

    }
  }

  void doSignalWorkflows(SignalWorkflowsRequest request) throws ExecutionException, InterruptedException {
    ActivityExecutionContext context = Activity.getExecutionContext();

    WorkflowClientOptions options = this.workflowClient.getOptions();


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

      List<ListenableFuture<?>> futures = new ArrayList<>();

      ListWorkflowExecutionsResponse listResponse = this.workflowServiceStubs.blockingStub().listWorkflowExecutions(listRequest);
      for (WorkflowExecutionInfo executionInfo : listResponse.getExecutionsList()) {
        DataConverter dataConverterWithSignalContext = options.getDataConverter()
            .withContext(new WorkflowSerializationContext(options.getNamespace(), executionInfo.getExecution().getWorkflowId()));
        Optional<Payloads> inputArgs = dataConverterWithSignalContext.toPayloads(request.getPayload());

        SignalWorkflowExecutionRequest.Builder signalRequest = SignalWorkflowExecutionRequest.newBuilder()
            .setRequestId(UUID.randomUUID().toString())
            .setWorkflowExecution(executionInfo.getExecution())
            .setNamespace(this.workflowClient.getOptions().getNamespace())
            .setIdentity(this.workflowClient.getOptions().getIdentity())
            .setHeader(HeaderUtils.toHeaderGrpc(Header.empty(), null))
            .setSignalName(request.getSignalName());

        inputArgs.ifPresent(signalRequest::setInput);

        var future = this.workflowServiceStubs.futureStub().signalWorkflowExecution(signalRequest.build());
        futures.add(future);
      }

      Futures.allAsList(futures).get();

      nextPageToken = listResponse.getNextPageToken();
      context.heartbeat(nextPageToken);

    } while (!nextPageToken.isEmpty());
  }
}
