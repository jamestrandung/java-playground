package com.james.playground.temporal.dsl.language.versioning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;

@JsonTypeInfo(
    use = Id.NAME,
    property = NodeChangeSignalType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DelayInterruptionSignal.class, name = NodeChangeSignalType.DELAY_INTERRUPTION),
})
public interface NodeChangeSignal {
  @JsonIgnore
  String getType();

  void accept(DelegatingVisitor visitor);

  Long getTargetGroupId();
}
