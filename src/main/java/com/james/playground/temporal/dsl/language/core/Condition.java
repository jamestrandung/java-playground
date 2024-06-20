package com.james.playground.temporal.dsl.language.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.SwitchVisitor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@FieldNameConstants
@JsonTypeInfo(
    use = Id.NAME,
    property = ConditionType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupMembershipCondition.class, name = ConditionType.GROUP_MEMBERSHIP),
})
public abstract class Condition {
  public abstract boolean accept(SwitchVisitor visitor);
}
