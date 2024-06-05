package com.james.playground.temporal.moneytransfer.activities;

import com.james.playground.service.DummyService;
import io.temporal.activity.Activity;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = AccountActivity.QUEUE_NAME)
public class AccountActivityImpl implements AccountActivity {
  // We can inject dependencies as usual by
  // marking an Activity as @Component
  @Autowired
  private DummyService dummyService;

  @Override
  public void withdraw(String referenceId, String accountId, int amount, int delayInSeconds) {
    dummyService.say("I'm withdrawing money");

    if (delayInSeconds > 0) {
      log.info("Delaying withdrawal of {} from account {}, reference ID: {} ", amount, accountId, referenceId);

      try {
        Thread.sleep(delayInSeconds * 1000L);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw Activity.wrap(new RuntimeException("Activity was interrupted"));
      }
    }

    log.info("Withdrawn {} from account {}, reference ID: {}", amount, accountId, referenceId);
  }

  @Override
  public void deposit(String referenceId, String accountId, int amount, boolean shouldSucceed) {
    if (shouldSucceed) {
      log.info("Deposited {} to account {}, reference ID: {}", amount, accountId, referenceId);
      return;
    }

    log.error("Failed to deposit {} to account {}, reference ID: {}", amount, accountId, referenceId);

    throw Activity.wrap(new RuntimeException("Simulated Activity error during deposit of funds"));
  }

  @Override
  public void depositV2(String referenceId, String accountId, int amount, boolean shouldSucceed) {
    if (shouldSucceed) {
      log.info("Deposited v2 {} to account {}, reference ID: {}", amount, accountId, referenceId);
      return;
    }

    log.error("Failed to deposit v2 {} to account {}, reference ID: {}", amount, accountId, referenceId);

    // Use ApplicationFailure to indicate non-retryable failure and overwrite retry policy from Workflow
    throw ApplicationFailure.newNonRetryableFailure("Simulated Activity error during deposit of funds", "DEPOSIT_FAILURE");
  }
}
