package com.james.playground.temporal.moneytransfer.activities;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface AccountActivity extends BaseActivity {
  String QUEUE_NAME = "MoneyTransferTaskQueue";
}
