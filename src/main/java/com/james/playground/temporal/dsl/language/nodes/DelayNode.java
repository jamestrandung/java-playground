package com.james.playground.temporal.dsl.language.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
  // Delay by duration
  private int durationInSeconds;

  // Delay by date time
  private String releaseDateTime; // ISO format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
  private String releaseTimeOfDay; // HH:mm
  private boolean shouldReleaseInUserTimezone;

  private long groupIdForActiveUsers;

  @Override
  public String accept(DelegatingVisitor visitor) {
    return visitor.visit(this);
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
  public LocalTime getReleaseLocalTime() {
    return LocalTime.parse(this.releaseTimeOfDay, DateTimeFormatter.ISO_LOCAL_TIME);
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

    public static boolean requireImmediateIntervention(DelayInterruptionSignal signal) {
      return signal != null && signal.getType() != DelayInterruptionType.CONFIG_MODIFIED;
    }
  }
}
