package com.james.playground.temporal.child;

import io.temporal.common.SearchAttributeKey;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.List;

@WorkflowImpl(taskQueues = {ChildWorkflow.QUEUE_NAME})
public class ChildWorkflowImpl implements ChildWorkflow {
  @Override
  public void doSomethingElse() {
    Workflow.upsertTypedSearchAttributes(SearchAttributeKey.forKeywordList("CustomKeywordList").valueSet(List.of("abc", "def")));

    Workflow.sleep(Duration.ofHours(2));
  }
}
