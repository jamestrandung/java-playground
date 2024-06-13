package com.james.playground.temporal.dsl.dto;

import com.james.playground.temporal.dsl.language.core.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MarketingWorkflowContext {
  protected Condition enrollmentCondition;
  protected Condition exitCondition;
  protected boolean shouldWithdrawIfEnrollmentConditionNotMet;
}
