package com.james.playground.controller.temporal;

import com.james.playground.temporal.signal.SignalReceiverWorkflow;
import com.james.playground.temporal.signal.SignalSenderWorkflow;
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
@RequestMapping("/temporal/signal")
public class SignalController {
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/receiver")
  public void createReceiver(@RequestParam String workflowIdSuffix) {
    SignalReceiverWorkflow workflow = workflowClient.newWorkflowStub(
        SignalReceiverWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(SignalReceiverWorkflow.QUEUE_NAME)
            .setWorkflowId(SignalReceiverWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
            .build()
    );

    WorkflowClient.execute(workflow::waitForSignal)
        .thenAccept(result -> {
          log.info("Signal received: {}", result);
        })
        .exceptionally(ex -> {
          log.error("Signal receiver failed with error: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/sender")
  public void createSender(@RequestParam String workflowIdSuffix, @RequestParam String message) {
    SignalSenderWorkflow workflow = workflowClient.newWorkflowStub(
        SignalSenderWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(SignalSenderWorkflow.QUEUE_NAME)
            .setWorkflowId(SignalSenderWorkflow.WORKFLOW_ID)
            .build()
    );

    WorkflowClient.execute(workflow::sendSignal, workflowIdSuffix, message)
        .thenAccept(result -> {
          log.info("Signal sent: {}", result);
        })
        .exceptionally(ex -> {
          log.error("Signal sender failed with error: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/direct")
  public void sendSignal(@RequestParam String workflowIdSuffix, @RequestParam String message) {
    // If WorkflowId does not exist or the workflow is not running, we will get an error.
    workflowClient.newWorkflowStub(SignalReceiverWorkflow.class, SignalReceiverWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
        .unblock(message);

    log.info("Signal sent directly");
  }
}
