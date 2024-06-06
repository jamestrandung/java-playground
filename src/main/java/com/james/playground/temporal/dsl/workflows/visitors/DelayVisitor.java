package com.james.playground.temporal.dsl.workflows.visitors;

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
public class DelayVisitor extends BaseVisitor implements DynamicVisitor<DelayNode> {
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
    Duration effectiveDuration = this.decideDelayDuration();

    if (effectiveDuration.isZero() || effectiveDuration.isNegative()) {
      return this.activeNode.getNextNodeId();
    }

    this.delayStartTimestamp = this.isFirstVisit() ? Workflow.currentTimeMillis() : this.delayStartTimestamp;
    this.interruptionSignal = null;

    log.info("Sleeping for {} seconds", effectiveDuration.toSeconds());

    Workflow.await(effectiveDuration, () -> DelayInterruptionSignal.requireImmediateIntervention(this.interruptionSignal));

    if (this.interruptionSignal != null) {
      return this.doDelay();
    }

    return this.activeNode.getNextNodeId();
  }

  Duration decideDelayDuration() {
    // Current node is up-to-date and this must be the 1st visit
    if (this.interruptionSignal == null) {
      return this.activeNode.getDuration();
    }

    if (DelayInterruptionSignal.hasImmediateReleaseSignal(this.interruptionSignal)) {
      return Duration.ZERO;
    }

    // Something changed
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

    this.interruptionSignal = signal;

    // TODO-1: handle the scenario where signal arrives just
    // after user was added to group but before delay starts.

    // TODO-2: handle the scenario where signal arrives late
    // just after the user has finished the delay.

    // TODO-3: handle the scenario where signal processing
    // starts just before the Activity for adding user to
    // group completes. Hence, user doesn't get this signal
    // and proceed to wait for the original delay after
    // getting added to group.

    // TOTHINK-1: might have to fire a signal to ALL users
    // with active workflows instead of just the user who
    // are sleeping at a particular node.
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
