package com.james.playground.temporal.dsl.language.conditions;

import com.james.playground.temporal.dsl.language.core.Condition;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.SwitchVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupMembershipCondition extends Condition {
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
