package com.james.playground.temporal.dsl.language.conditions;

import com.james.playground.temporal.dsl.language.Condition;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.BranchVisitor;
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

  @Override
  public boolean accept(BranchVisitor visitor) {
    return visitor.visit(this);
  }
}
