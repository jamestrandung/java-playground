package com.james.playground.temporal.moneytransfer.workflows;

import com.james.playground.temporal.moneytransfer.activities.AccountActivity;
import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import com.james.playground.temporal.utils.VersionResolver;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = MoneyTransferWorkflow.QUEUE_NAME)
public class MoneyTransferWorkflowImpl implements MoneyTransferWorkflow {

  // Inside Workflow implementation, we MUST use Workflow.getLogger to get a logger instance
  // Inside Activity implementation, we can use Lombok logger as usual
  private static final Logger logger = Workflow.getLogger(MoneyTransferWorkflowImpl.class);

  private static final RetryOptions RETRY_OPTIONS = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(1)) // Wait duration before first retry
      .setMaximumInterval(Duration.ofSeconds(60)) // Maximum wait duration between retries
      .setMaximumAttempts(3) // Maximum number of retry attempts
      .setBackoffCoefficient(1)
      .build();

  private static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
      // One of the following MUST be set, StartToCloseTimeout is a better option
      .setScheduleToCloseTimeout(Duration.ofSeconds(60)) // Max duration from scheduled to completion, including queue time
      .setStartToCloseTimeout(Duration.ofSeconds(10)) // Max execution time for single Activity
      .setRetryOptions(RETRY_OPTIONS)
      .build();

  private final AccountActivity accountActivity = Workflow.newActivityStub(AccountActivity.class, ACTIVITY_OPTIONS, null);

  @Override
  public String transfer(TransactionDetails details) {
    if (details.isShouldFailImmediately()) {
      // Will cause the workflow to be retried indefinitely by default
      throw new RuntimeException("Simulated immediate failure");
    }

    VersionResolver versions = VersionResolver.from(Map.of(ChangeId.CHANGE_DEPOSIT_METHOD, 1));

    try {
      Promise<Void> promise = Async.procedure(
          this.accountActivity::withdraw,
          details.getReferenceId(),
          details.getSourceAccountId(),
          details.getAmountToTransfer(),
          details.getWithdrawDelayInSeconds()
      );

      promise.get();

    } catch (Exception ex) {
      logger.error("Transaction failed, error: {}", ex.getMessage());
      return "WITHDRAW_FAILED";
    }

    try {
      // Non-deterministic errors will keep the workflow in the
      // Running state. It's possible to redeploy the Worker with
      // a fix and resume the workflow automatically.
      if (versions.get(ChangeId.CHANGE_DEPOSIT_METHOD) == 1) {
        this.accountActivity.depositV2(
            details.getReferenceId(),
            details.getDestinationAccountId(),
            details.getAmountToTransfer(),
            details.isShouldSucceed()
        );
      } else {
        this.accountActivity.deposit(
            details.getReferenceId(),
            details.getDestinationAccountId(),
            details.getAmountToTransfer(),
            details.isShouldSucceed()
        );
      }

      //      this.accountActivity.deposit(
      //          details.getReferenceId(),
      //          details.getDestinationAccountId(),
      //          details.getAmountToTransfer(),
      //          details.isShouldSucceed()
      //      );

      logger.info("Transaction completed");
      return "TRANSFER_COMPLETED_" + details.getAmountToTransfer();

    } catch (Exception ex) {
      logger.error("Transaction failed, error: {}", ex.getMessage());
    }

    try {
      if (versions.get(ChangeId.CHANGE_DEPOSIT_METHOD) == 1) {
        this.accountActivity.depositV2(
            details.getReferenceId(),
            details.getSourceAccountId(),
            details.getAmountToTransfer(),
            details.isShouldCompensationSucceed()
        );
      } else {
        this.accountActivity.deposit(
            details.getReferenceId(),
            details.getSourceAccountId(),
            details.getAmountToTransfer(),
            details.isShouldCompensationSucceed()
        );
      }

      logger.info("Failure compensated");
      return "FAILURE_COMPENSATED";

    } catch (Exception ex) {
      if (details.isShouldSwallowFailure()) {
        logger.info("Failure swallowed");
        return "FAILURE_SWALLOWED";
      }

      logger.error("Compensation failed, error: {}", ex.getMessage());
      throw ex;
    }
  }
}
