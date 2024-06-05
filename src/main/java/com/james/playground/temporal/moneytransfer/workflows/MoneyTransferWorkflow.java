package com.james.playground.temporal.moneytransfer.workflows;

import com.james.playground.temporal.moneytransfer.dto.TransactionDetails;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MoneyTransferWorkflow {
  String QUEUE_NAME = "MoneyTransferTaskQueue";
  String WORKFLOW_ID = "money-transfer-workflow";

  @WorkflowMethod
  String transfer(TransactionDetails details);
}
