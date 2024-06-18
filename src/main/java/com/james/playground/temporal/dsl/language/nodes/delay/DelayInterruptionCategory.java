package com.james.playground.temporal.dsl.language.nodes.delay;

public enum DelayInterruptionCategory {
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
