package com.james.playground.controller.temporal;

import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.moneytransfer.workflows.MoneyTransferWorkflow;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/temporal")
public class MoneyTransferWithVersioningController {
  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping("/transfer/execute")
  public void transferWithExecute(@RequestBody TransactionDetails details) {
    MoneyTransferWorkflow moneyTransferWorkflow = this.workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MoneyTransferWorkflow.QUEUE_NAME)
            .setWorkflowId(MoneyTransferWorkflow.WORKFLOW_ID)
            .build()
    );

    // Duplicated workflow ID will return the same result, no error
    WorkflowClient.execute(moneyTransferWorkflow::transfer, details)
        .thenAccept(result -> {
          log.info("Money transfer completed with result: {}", result);
        })
        .exceptionally(ex -> {
          log.info("Money transfer failed with error: {}", ex.getMessage());
          return null;
        });
  }

  @PostMapping("/transfer/start")
  public void transferWithStart(@RequestBody TransactionDetails details) {
    MoneyTransferWorkflow moneyTransferWorkflow = this.workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MoneyTransferWorkflow.QUEUE_NAME)
            .setWorkflowId(MoneyTransferWorkflow.WORKFLOW_ID)
            .build()
    );

    // Duplicated workflow ID will cause exception
    WorkflowExecution execution = WorkflowClient.start(moneyTransferWorkflow::transfer, details);

    log.info("Money transfer started with run ID: {}", execution.getRunId());

    log.info("Money transfer completed with result: {}", moneyTransferWorkflow.transfer(details));
  }

  @PostMapping("/transfer/sync")
  public void transferSync(@RequestBody TransactionDetails details) {
    MoneyTransferWorkflow moneyTransferWorkflow = this.workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MoneyTransferWorkflow.QUEUE_NAME)
            .setWorkflowId(MoneyTransferWorkflow.WORKFLOW_ID)
            .build()
    );

    log.info("Money transfer completed with result: {}", moneyTransferWorkflow.transfer(details));
  }
}
