package com.james.playground.temporal.dsl.workflows.core;


import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@WorkflowInterface
public interface GroupSignalBroadcastWorkflow {
  String QUEUE_NAME = "GroupSignalBroadcastTaskQueue";
  String WORKFLOW_ID = "group-signal-broadcast-workflow";

  @WorkflowMethod
  <T> void broadcastSignalToGroup(GroupSignalInput<T> input);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class GroupSignalInput<T> {
    private Long groupId;
    private String workflowIdPrefix;
    private String signalName;
    private T payload;
    private int pageIdx;
    private int pageSize;
  }
}
