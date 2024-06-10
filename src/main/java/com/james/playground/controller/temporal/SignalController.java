package com.james.playground.controller.temporal;

import com.james.playground.temporal.dsl.workflows.core.GroupSignalBroadcastWorkflow;
import com.james.playground.temporal.signal.SignalReceiverWorkflow;
import com.james.playground.temporal.signal.SignalSenderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    SignalReceiverWorkflow workflow = this.workflowClient.newWorkflowStub(
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
    SignalSenderWorkflow workflow = this.workflowClient.newWorkflowStub(
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
    this.workflowClient.newWorkflowStub(SignalReceiverWorkflow.class, SignalReceiverWorkflow.WORKFLOW_ID_PREFIX + workflowIdSuffix)
        .unblock(message);

    log.info("Signal sent directly");
  }

  @PostMapping("/group")
  public void broadcastSignalToGroup(@RequestParam int count) {
    this.startSignalReceivers(count);

    GroupSignalBroadcastWorkflow workflow = this.workflowClient.newWorkflowStub(
        GroupSignalBroadcastWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(GroupSignalBroadcastWorkflow.QUEUE_NAME)
            .setWorkflowId(GroupSignalBroadcastWorkflow.WORKFLOW_ID)
            .build()
    );

    workflow.broadcastSignalToGroup(
        GroupSignalBroadcastWorkflow.GroupSignalInput.builder()
            .groupId(1L)
            .workflowIdPrefix(SignalReceiverWorkflow.WORKFLOW_ID_PREFIX)
            .signalName("unblock")
            .payload("Hello, World!")
            .pageIdx(0)
            .pageSize(count / 2)
            .build()
    );
  }

  void startSignalReceivers(int count) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      SignalReceiverWorkflow workflow = this.workflowClient.newWorkflowStub(
          SignalReceiverWorkflow.class,
          WorkflowOptions.newBuilder()
              .setTaskQueue(SignalReceiverWorkflow.QUEUE_NAME)
              .setWorkflowId(SignalReceiverWorkflow.WORKFLOW_ID_PREFIX + i)
              .build()
      );

      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        WorkflowClient.start(workflow::waitForSignal);
      });

      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }
}
