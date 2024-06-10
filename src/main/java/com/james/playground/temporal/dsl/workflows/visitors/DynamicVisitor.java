package com.james.playground.temporal.dsl.workflows.visitors;

import com.james.playground.temporal.dsl.language.WorkflowNode;

public interface DynamicVisitor<T extends WorkflowNode> {
  WorkflowNode visit(T node);
}
