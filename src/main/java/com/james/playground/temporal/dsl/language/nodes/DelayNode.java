package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DelayNode extends WorkflowNode {
  private int durationInSeconds;
  private long groupIdForActiveUsers;

  @Override
  public WorkflowNode accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }

  @JsonIgnore
  public Duration getDuration() {
    return this.durationInSeconds <= 0 ? Duration.ZERO : Duration.ofSeconds(this.durationInSeconds);
  }

  public enum DelayInterruptionType {
    // This type should be used ONLY when there's no change to the node
    // itself but the users at this node must move on immediately.
    IMMEDIATE_RELEASE,
    // This type should be used when there's a change to the duration of
    // the node including node deletion.
    DURATION_MODIFIED,
    // This type should be used when there's an important change to the
    // node that affects execution path (e.g. next node ID changed) that
    // does not require immediate intervention.
    CONFIG_MODIFIED
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DelayInterruptionSignal {
    private DelayInterruptionType type;
    private String affectedNodeId;

    public static boolean hasImmediateReleaseSignal(DelayInterruptionSignal signal) {
      return signal != null && signal.getType() == DelayInterruptionType.IMMEDIATE_RELEASE;
    }

    public static boolean hasConfigModifiedSignal(DelayInterruptionSignal signal) {
      return signal != null && signal.getType() == DelayInterruptionType.CONFIG_MODIFIED;
    }

    public static boolean requireImmediateIntervention(DelayInterruptionSignal signal) {
      if (signal == null) {
        return false;
      }

      return signal.getType() != DelayInterruptionType.CONFIG_MODIFIED;
    }
  }
}
