package com.james.playground.temporal.dsl.language.core;

public interface WorkflowDefinition {
  WorkflowNode findNodeIgnoringDeletedNodes(String nodeId);

  WorkflowNode findNodeAcceptingDeletedNode(String nodeId);
}
