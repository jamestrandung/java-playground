package com.james.playground.temporal.moneytransfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails {
  private String referenceId;
  private String sourceAccountId;
  private String destinationAccountId;
  private int amountToTransfer;
  private int withdrawDelayInSeconds;
  private boolean shouldSucceed;
  private boolean shouldCompensationSucceed;
  private boolean shouldSwallowFailure;
  private boolean shouldFailImmediately;
}
