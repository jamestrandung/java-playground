package com.james.playground.temporal.dsl.language.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.Condition;
import com.james.playground.temporal.dsl.language.core.ConditionType;
import com.james.playground.temporal.dsl.language.core.ConditionType.Constants;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.SwitchVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembershipCondition implements Condition {
  @JsonProperty(Constants.PROPERTY_NAME)
  private final ConditionType type = ConditionType.GROUP_MEMBERSHIP;

  private Long groupId;
  private boolean shouldBeInGroup;

  public static GroupMembershipCondition isInGroup(Long groupId) {
    return GroupMembershipCondition.builder()
        .groupId(groupId)
        .shouldBeInGroup(true)
        .build();
  }

  public static GroupMembershipCondition isNotInGroup(Long groupId) {
    return GroupMembershipCondition.builder()
        .groupId(groupId)
        .shouldBeInGroup(false)
        .build();
  }

  @Override
  public boolean accept(SwitchVisitor visitor) {
    return visitor.visit(this);
  }
}
