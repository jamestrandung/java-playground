package com.james.playground.controller.temporal;

import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.moneytransfer.workflows.MoneyTransferWorkflow;
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

  @PostMapping("/transfer")
  public void transfer(@RequestBody TransactionDetails details) {
    MoneyTransferWorkflow moneyTransferWorkflow = workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        WorkflowOptions.newBuilder()
            .setTaskQueue(MoneyTransferWorkflow.QUEUE_NAME)
            .setWorkflowId(MoneyTransferWorkflow.WORKFLOW_ID)
            .build()
    );

    //    log.info("Money transfer completed with result: {}", moneyTransferWorkflow.transfer(details));

    WorkflowClient.execute(moneyTransferWorkflow::transfer, details)
        .thenAccept(result -> {
          log.info("Money transfer completed with result: {}", result);
        })
        .exceptionally(ex -> {
          log.info("Money transfer failed with error: {}", ex.getMessage());
          return null;
        });
  }
}
