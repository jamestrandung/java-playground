package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.DynamicWorkflowImpl;
import java.time.Duration;
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
public class DelayNode extends WorkflowNode {
  private int durationInSeconds;

  @Override
  public WorkflowNode accept(DynamicWorkflowImpl visitor) {
    return visitor.visit(this);
  }

  @JsonIgnore
  public Duration getDuration() {
    return this.durationInSeconds <= 0 ? Duration.ZERO : Duration.ofSeconds(this.durationInSeconds);
  }

  public enum DelayInterruptionType {
    IMMEDIATE_RELEASE,
    DURATION_MODIFIED
  }
}
