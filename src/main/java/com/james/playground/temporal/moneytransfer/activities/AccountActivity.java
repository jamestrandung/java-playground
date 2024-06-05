package com.james.playground.temporal.moneytransfer.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface AccountActivity {
  String QUEUE_NAME = "MoneyTransferTaskQueue";

  @ActivityMethod
  void withdraw(String referenceId, String accountId, int amount, int delayInSeconds);

  @ActivityMethod
  void deposit(String referenceId, String accountId, int amount, boolean shouldSucceed);

  @ActivityMethod
  void depositV2(String referenceId, String accountId, int amount, boolean shouldSucceed);
}
