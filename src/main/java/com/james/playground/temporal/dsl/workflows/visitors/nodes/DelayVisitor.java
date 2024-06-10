package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode;
import com.james.playground.temporal.dsl.language.nodes.DelayNode.DelayInterruptionSignal;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.time.Instant;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class DelayVisitor extends NodeVisitor<DelayNode> {
  private static final Logger logger = Workflow.getLogger(DelayVisitor.class);

  private DelayNode activeNode;
  private long delayStartTimestamp;
  private DelayInterruptionSignal interruptionSignal;

  public DelayVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public WorkflowNode visit(DelayNode node) {
    log.info("DelayNode: {}", node);

    this.activeNode = node;

    this.userGroupActivity.addToGroup(
        UserGroupInput.builder()
            .userId(this.input.getUserId())
            .groupId(this.activeNode.getGroupIdForActiveUsers())
            .build()
    );

    String nextNodeId = this.doDelay();

    this.userGroupActivity.removeFromGroup(
        UserGroupInput.builder()
            .userId(this.input.getUserId())
            .groupId(this.activeNode.getGroupIdForActiveUsers())
            .build()
    );

    this.resetMarkers();

    return this.findNodeIgnoringDeletedNodes(nextNodeId);
  }

  String doDelay() {
    Duration duration = this.handleSignalAndDecideDelayDuration();

    if (duration.isZero() || duration.isNegative()) {
      return this.activeNode.getNextNodeId();
    }

    this.delayStartTimestamp = this.isFirstVisit() ? Workflow.currentTimeMillis() : this.delayStartTimestamp;
    this.interruptionSignal = null;

    log.info("Sleeping for {} seconds", duration.toSeconds());

    Workflow.await(duration, () -> DelayInterruptionSignal.requireImmediateIntervention(this.interruptionSignal));

    if (this.interruptionSignal != null) {
      return this.doDelay();
    }

    return this.activeNode.getNextNodeId();
  }

  Duration handleSignalAndDecideDelayDuration() {
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

    if (this.activeNode.isDeleted()) {
      return Duration.ZERO;
    }

    if (this.isFirstVisit()) {
      return this.activeNode.getDuration();
    }

    // Calculate remaining duration for subsequent visits
    long     now             = Workflow.currentTimeMillis();
    Duration elapsedDuration = Duration.between(Instant.ofEpochMilli(this.delayStartTimestamp), Instant.ofEpochMilli(now));

    log.info("Already slept for {} seconds", elapsedDuration.toSeconds());
    return this.activeNode.getDuration().minus(elapsedDuration);
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
}
