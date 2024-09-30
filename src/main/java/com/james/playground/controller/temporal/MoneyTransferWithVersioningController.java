package com.james.playground.controller.temporal;

import com.james.playground.temporal.interceptor.RetryOnSignalInterceptorListener;
import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.moneytransfer.workflows.MoneyTransferWorkflow;
import com.james.playground.temporal.utils.ExceptionUtils;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    try {
      // Duplicated workflow ID will cause exception
      WorkflowExecution execution = WorkflowClient.start(moneyTransferWorkflow::transfer, details);

      log.info("Money transfer started with run ID: {}", execution.getRunId());

    } catch (WorkflowExecutionAlreadyStarted ex) {
      log.info("Money transfer already started with run ID: {}", ex.getExecution().getRunId());

    } catch (Exception ex) {
      log.info(ExceptionUtils.getStackTrace(ex));
    }

    // This will not cause exception if the workflow ID is duplicated
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

    // This will not cause exception if the workflow ID is duplicated
    log.info("Money transfer completed with result: {}", moneyTransferWorkflow.transfer(details));
  }

  @PostMapping("/transfer/retry")
  public void retry(@RequestParam boolean shouldRetry) {
    var stub = this.workflowClient.newWorkflowStub(RetryOnSignalInterceptorListener.class, MoneyTransferWorkflow.WORKFLOW_ID);
    if (shouldRetry) {
      stub.retry();
      return;
    }

    stub.fail();
  }
}
