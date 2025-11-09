package com.james.playground.temporal.moneytransfer.activities;

import io.temporal.activity.ActivityMethod;

public interface BaseActivity {
  @ActivityMethod
  void withdraw(String referenceId, String accountId, int amount, int delayInSeconds);

  @ActivityMethod
  void deposit(String referenceId, String accountId, int amount, boolean shouldSucceed);

  @ActivityMethod
  void depositV2(String referenceId, String accountId, int amount, boolean shouldSucceed);

  @ActivityMethod
  void warn(String errMessage);
}
