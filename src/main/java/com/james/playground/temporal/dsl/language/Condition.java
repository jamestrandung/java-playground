package com.james.playground.temporal.dsl.language;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.BranchVisitor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonTypeInfo(
    use = Id.NAME,
    property = ConditionType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupMembershipCondition.class, name = ConditionType.GROUP_MEMBERSHIP),
})
public abstract class Condition {
  private String nextNodeId;

  public abstract boolean accept(BranchVisitor visitor);
}
