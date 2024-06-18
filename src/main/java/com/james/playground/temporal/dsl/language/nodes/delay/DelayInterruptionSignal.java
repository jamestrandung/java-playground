package com.james.playground.temporal.dsl.language.nodes.delay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignalType;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayInterruptionSignal implements NodeChangeSignal {
  // https://stackoverflow.com/questions/34193177/why-does-jackson-polymorphic-serialization-not-work-in-lists
  @JsonProperty(NodeChangeSignalType.PROPERTY_NAME)
  private final String type = NodeChangeSignalType.DELAY_INTERRUPTION;

  private DelayInterruptionCategory category;
  private String affectedNodeId;
  private Long targetGroupId;

  public static DelayInterruptionSignal create(DelayInterruptionCategory category, String affectedNodeId, Long targetGroupId) {
    return DelayInterruptionSignal.builder()
        .category(category)
        .affectedNodeId(affectedNodeId)
        .targetGroupId(targetGroupId)
        .build();
  }

  public static boolean hasImmediateReleaseSignal(DelayInterruptionSignal signal) {
    return signal != null && signal.getCategory() == DelayInterruptionCategory.IMMEDIATE_RELEASE;
  }

  public static boolean requireImmediateIntervention(DelayInterruptionSignal signal) {
    return signal != null && signal.getCategory() != DelayInterruptionCategory.CONFIG_MODIFIED;
  }

  @Override
  public void accept(DelegatingVisitor visitor) {
    visitor.visit(this);
  }
}
