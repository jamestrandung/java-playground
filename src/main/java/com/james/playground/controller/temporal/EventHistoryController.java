package com.james.playground.controller.temporal;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryRequest;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryResponse;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal")
public class EventHistoryController {
  @Autowired
  private WorkflowClient workflowClient;

  @GetMapping("/history")
  public String fetchEventHistory(@RequestParam String workflowId) throws InvalidProtocolBufferException {
    WorkflowExecution execution = WorkflowExecution.newBuilder()
        .setWorkflowId(workflowId)
        .build();

    GetWorkflowExecutionHistoryRequest request = GetWorkflowExecutionHistoryRequest.newBuilder()
        .setNamespace("default")
        .setExecution(execution)
        .build();

    GetWorkflowExecutionHistoryResponse result = workflowClient.getWorkflowServiceStubs()
        .blockingStub()
        .getWorkflowExecutionHistory(request);

    return JsonFormat.printer().print(result.getHistory());
  }
}
