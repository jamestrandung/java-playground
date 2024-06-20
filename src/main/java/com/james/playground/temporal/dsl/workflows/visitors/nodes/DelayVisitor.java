package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserActivity.UserInfoInput;
import com.james.playground.temporal.dsl.activities.UserActivity.UserInfoOutput;
import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayInterruptionSignal;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayNode;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class DelayVisitor extends NodeVisitor<DelayNode> {
  private static final Logger LOGGER = Workflow.getLogger(DelayVisitor.class);
  private Supplier<WorkflowDefinition<?>> workflowDefinitionSupplier;

  private ZoneId userTimezone;
  private DelayNode activeNode;
  private long delayStartTimestamp;
  private DelayInterruptionSignal interruptionSignal;

  public DelayVisitor(
      DynamicWorkflowInput input,
      Supplier<WorkflowDefinition<?>> workflowDefinitionSupplier
  ) {
    super(input);
    this.workflowDefinitionSupplier = workflowDefinitionSupplier;
  }

  @Override
  public String visit(DelayNode node) {
    try {
      log.info("DelayNode: {}", node);

      this.activeNode = node;

      // Move on immediately if possible without
      // executing user group activities
      Duration duration = this.decideDelayDuration();
      if (duration.isZero() || duration.isNegative()) {
        return this.activeNode.getNextNodeId();
      }

      this.localUserGroupActivity.addToGroup(
          UserGroupInput.builder()
              .userId(this.input.getUserId())
              .groupId(this.activeNode.getActiveGroupId())
              .build()
      );

      String nextNodeId = this.doDelay(false);

      this.localUserGroupActivity.removeFromGroup(
          UserGroupInput.builder()
              .userId(this.input.getUserId())
              .groupId(this.activeNode.getActiveGroupId())
              .build()
      );

      return nextNodeId;

    } finally {
      this.resetMarkers();
    }
  }

  String doDelay(boolean hasCompletedFullDelay) {
    Duration duration = this.handleSignalAndDecideDelayDuration(hasCompletedFullDelay);

    if (duration.isZero() || duration.isNegative()) {
      return this.activeNode.getNextNodeId();
    }

    this.delayStartTimestamp = this.isFirstVisit() ? Workflow.currentTimeMillis() : this.delayStartTimestamp;
    this.interruptionSignal = null;

    log.info("Sleeping for {} seconds", duration.toSeconds());

    boolean isInterruptedBySignal = Workflow.await(
        duration,
        () -> DelayInterruptionSignal.requireImmediateIntervention(this.interruptionSignal)
    );

    if (this.interruptionSignal != null) {
      return this.doDelay(!isInterruptedBySignal);
    }

    return this.activeNode.getNextNodeId();
  }

  Duration handleSignalAndDecideDelayDuration(boolean hasCompletedFullDelay) {
    if (DelayInterruptionSignal.hasImmediateReleaseSignal(this.interruptionSignal)) {
      return Duration.ZERO;
    }

    // When a node gets updated, a Signal is sent to all users in the active
    // group. On the first visit, there's a chance that Signal processing
    // starts while we were adding the user to group. Hence, no Signal was
    // sent for this user because he was not in the group at that point.
    //
    // Hence, we need to fetch the node again on first visit as well as on
    // subsequent visits due to interruptionSignal.
    this.activeNode = (DelayNode) this.findNodeAcceptingDeletedNode(this.activeNode.getId());

    // If we already completed full delay, no need further waiting
    if (hasCompletedFullDelay || this.activeNode.isDeleted()) {
      return Duration.ZERO;
    }

    return this.decideDelayDuration();
  }

  Duration decideDelayDuration() {
    if (this.activeNode.isDelayByDuration()) {
      return this.computeBasicDelayDuration();
    }

    if (this.activeNode.isDelayByDateTime()) {
      return this.computeDelayDurationByDateTime();
    }

    if (this.activeNode.isDelayByTimeOfDay()) {
      return this.computeDelayDurationByTimeOfDay();
    }

    return Duration.ZERO;
  }

  Duration computeBasicDelayDuration() {
    if (this.isFirstVisit()) {
      return this.activeNode.getDuration();
    }

    // Calculate remaining duration for subsequent visits
    long     now             = Workflow.currentTimeMillis();
    Duration elapsedDuration = Duration.between(Instant.ofEpochMilli(this.delayStartTimestamp), Instant.ofEpochMilli(now));

    log.info("Already slept for {} seconds", elapsedDuration.toSeconds());
    return this.activeNode.getDuration().minus(elapsedDuration);
  }

  Duration computeDelayDurationByDateTime() {
    ZonedDateTime zonedDateTime = this.activeNode.getReleaseZonedDateTime();
    if (this.activeNode.isShouldReleaseInUserTimezone()) {
      ZoneId        timezone      = this.fetchUserTimezoneOnceAndCache();
      LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

      zonedDateTime = localDateTime.atZone(timezone);
    }

    return Duration.between(Instant.ofEpochMilli(Workflow.currentTimeMillis()), zonedDateTime);
  }

  Duration computeDelayDurationByTimeOfDay() {
    OffsetTime time = this.activeNode.getReleaseOffsetTime();

    ZoneId timezone = time.getOffset();
    if (this.activeNode.isShouldReleaseInUserTimezone()) {
      timezone = this.fetchUserTimezoneOnceAndCache();
    }

    ZonedDateTime nowDateTime    = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Workflow.currentTimeMillis()), timezone);
    ZonedDateTime targetDateTime = nowDateTime.withHour(time.getHour()).withMinute(time.getMinute());

    log.info("Now: {}", nowDateTime);
    log.info("Target: {}", targetDateTime);

    if (targetDateTime.isBefore(nowDateTime)) {
      targetDateTime = targetDateTime.plusDays(1);
    }

    // .withSecond(0) has to be done here because if we do it when we
    // create targetDateTime above, targetDateTime might fall behind
    // nowDateTime when now happens to be the configured release time.
    // E.g. release time is 10:05, now is 10:05:25. This allows us to
    // skip the delay immediately instead of waiting 1 full day.
    return Duration.between(nowDateTime, targetDateTime.withSecond(0));
  }

  ZoneId fetchUserTimezoneOnceAndCache() {
    if (this.userTimezone == null) {
      UserInfoOutput output = this.localUserActivity.getTimezone(
          UserInfoInput.builder()
              .userId(this.input.getUserId())
              .build()
      );

      this.userTimezone = output.getTimezone();
    }

    return this.userTimezone;
  }

  public void visit(DelayInterruptionSignal signal) {
    // We haven't arrived at the node yet or already passed it
    if (this.activeNode == null) {
      return;
    }

    // This signal is meant for another delay node
    if (!StringUtils.equals(this.activeNode.getId(), signal.getAffectedNodeId())) {
      return;
    }

    // If there's a pending signal that requires immediate intervention, don't overwrite it
    if (DelayInterruptionSignal.requireImmediateIntervention(this.interruptionSignal)) {
      return;
    }

    this.interruptionSignal = signal;
  }

  boolean isFirstVisit() {
    return this.delayStartTimestamp == 0;
  }

  void resetMarkers() {
    this.activeNode = null;
    this.delayStartTimestamp = 0;
    this.interruptionSignal = null;
  }

  WorkflowNode findNodeAcceptingDeletedNode(String nodeId) {
    return Workflow.sideEffect(
        WorkflowNode.class,
        () -> this.workflowDefinitionSupplier.get()
            .findNodeAcceptingDeletedNode(nodeId)
    );
  }
}
