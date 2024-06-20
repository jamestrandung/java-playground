package com.james.playground.temporal.dsl.dto;

import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MarketingContext {
  private GroupMembershipCondition enrollmentCondition;
  private GroupMembershipCondition exitCondition;
  private boolean shouldWithdrawIfEnrollmentConditionNotMet;
  private boolean shouldBeEjectedFromAllOtherWorkflows;

  private Long lifetimeGroupId;
  private Long activeGroupId;

  public void merge(MarketingContext latest) {
    this.enrollmentCondition = latest.enrollmentCondition;
    this.exitCondition = latest.exitCondition;
    this.shouldWithdrawIfEnrollmentConditionNotMet = latest.shouldWithdrawIfEnrollmentConditionNotMet;
  }
}
