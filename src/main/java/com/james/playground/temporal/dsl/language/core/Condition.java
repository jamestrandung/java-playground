package com.james.playground.temporal.dsl.language.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.language.core.ConditionType.Constants;
import com.james.playground.temporal.dsl.workflows.visitors.nodes.SwitchVisitor;

@JsonTypeInfo(
    use = Id.NAME,
    property = Constants.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupMembershipCondition.class, name = Constants.GROUP_MEMBERSHIP_VALUE),
})
public interface Condition {
  boolean accept(SwitchVisitor visitor);
}
