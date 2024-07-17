package com.james.playground.temporal.dsl.language.nodes.delay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.NodeType.Constants;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.time.Duration;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DelayNode extends WorkflowNode {
  @JsonProperty(Constants.PROPERTY_NAME)
  private final NodeType type = NodeType.DELAY;

  // Delay by duration
  private int durationInSeconds;

  // Delay by date time
  private String releaseDateTime; // ISO format: 2011-12-03T10:15:30+01:00
  // Delay until the very next time of day
  private String releaseTimeOfDay; // ISO format: 10:15:30+01:00
  // Whether to replace the timezone portion with the user's timezone
  private boolean shouldReleaseInUserTimezone;

  private long activeGroupId;

  @Override
  public String accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public Optional<NodeChangeSignal> detectChange(WorkflowNode latest) {
    if (latest instanceof DelayNode casted) {
      if (this.isDeleted() != casted.isDeleted()
          || this.durationInSeconds != casted.durationInSeconds
          || !StringUtils.equals(this.releaseDateTime, casted.releaseDateTime)
          || !StringUtils.equals(this.releaseTimeOfDay, casted.releaseTimeOfDay)
          || this.shouldReleaseInUserTimezone != casted.shouldReleaseInUserTimezone
      ) {
        return Optional.of(
            DelayInterruptionSignal.create(DelayInterruptionCategory.DURATION_MODIFIED, this.getId(), this.activeGroupId)
        );
      }

      if (!StringUtils.equals(this.getNextNodeId(), casted.getNextNodeId())) {
        return Optional.of(
            DelayInterruptionSignal.create(DelayInterruptionCategory.CONFIG_MODIFIED, this.getId(), this.activeGroupId)
        );
      }
    }

    // By right, nodes can never change type while keeping the same ID.
    // In case it happened, just respect the original configurations &
    // move on to the next node at the end of the delay.
    return Optional.empty();
  }

  @JsonIgnore
  public boolean isDelayByDuration() {
    return this.durationInSeconds > 0;
  }

  @JsonIgnore
  public Duration getDuration() {
    return this.durationInSeconds <= 0 ? Duration.ZERO : Duration.ofSeconds(this.durationInSeconds);
  }

  @JsonIgnore
  public boolean isDelayByDateTime() {
    return StringUtils.isNotBlank(this.releaseDateTime);
  }

  @JsonIgnore
  public ZonedDateTime getReleaseZonedDateTime() {
    return ZonedDateTime.parse(this.releaseDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  @JsonIgnore
  public boolean isDelayByTimeOfDay() {
    return StringUtils.isNotBlank(this.releaseTimeOfDay);
  }

  @JsonIgnore
  public OffsetTime getReleaseOffsetTime() {
    return OffsetTime.parse(this.releaseTimeOfDay, DateTimeFormatter.ISO_OFFSET_TIME);
  }
}
