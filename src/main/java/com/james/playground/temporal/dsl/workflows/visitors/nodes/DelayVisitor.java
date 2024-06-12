package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserActivity.UserInfoInput;
import com.james.playground.temporal.dsl.activities.UserActivity.UserInfoOutput;
import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.WorkflowStore;
import com.james.playground.temporal.dsl.language.nodes.DelayNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class DelayVisitor extends NodeVisitor<DelayNode> {
  private static final Logger LOGGER = Workflow.getLogger(DelayVisitor.class);
  private static final ZoneId SINGAPORE_TIMEZONE = ZoneId.of("Asia/Singapore");

  private ZoneId userTimezone;
  private DelayNode activeNode;
  private long delayStartTimestamp;
  private DelayInterruptionSignal interruptionSignal;

  public DelayVisitor(DynamicWorkflowInput input) {
    super(input);
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
              .groupId(this.activeNode.getGroupIdForActiveUsers())
              .build()
      );

      String nextNodeId = this.doDelay(false);

      this.localUserGroupActivity.removeFromGroup(
          UserGroupInput.builder()
              .userId(this.input.getUserId())
              .groupId(this.activeNode.getGroupIdForActiveUsers())
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

    ZoneId timezone = this.decideTimezone();

    if (this.activeNode.isDelayByDateTime()) {
      return this.computeDelayDurationByDateTime(timezone);
    }

    if (this.activeNode.isDelayByTimeOfDay()) {
      return this.computeDelayDurationByTimeOfDay(timezone);
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

  ZoneId decideTimezone() {
    if (!this.activeNode.isShouldReleaseInUserTimezone()) {
      return SINGAPORE_TIMEZONE;
    }

    return this.fetchUserTimezoneOnceAndCache();
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

  Duration computeDelayDurationByDateTime(ZoneId timezone) {
    ZonedDateTime zonedDateTime = this.activeNode.getReleaseZonedDateTime();
    if (this.activeNode.isShouldReleaseInUserTimezone()) {
      LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
      zonedDateTime = localDateTime.atZone(timezone);
    }

    return Duration.between(Instant.ofEpochMilli(Workflow.currentTimeMillis()), zonedDateTime);
  }

  Duration computeDelayDurationByTimeOfDay(ZoneId timezone) {
    LocalTime     time        = this.activeNode.getReleaseLocalTime();
    Instant       now         = Instant.ofEpochMilli(Workflow.currentTimeMillis());
    ZonedDateTime nowDateTime = ZonedDateTime.ofInstant(now, timezone);

    ZonedDateTime targetDateTime = nowDateTime.withHour(time.getHour()).withMinute(time.getMinute());
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

  public void interruptDelay(DelayInterruptionSignal signal) {
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

    // TODO-1: handle the scenario where signal arrives just
    // after user was added to group but before delay starts.
    // => Need to queue the signal for later processing.

    // TODO-2: handle the scenario where signal arrives late
    // just after the user has finished the delay.
    // => Need to discard the signal.

    // TODO-3: handle the scenario where signal processing
    // starts just before the Activity for adding user to
    // group completes. Hence, user doesn't get this signal
    // and proceed to wait for the original delay after
    // getting added to group.
    // => Need to detect changes.
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
        () -> WorkflowStore.getInstance().findNodeAcceptingDeletedNode(this.input.getWorkflowDefinitionId(), nodeId)
    );
  }
}
