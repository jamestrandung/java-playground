package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.utils.WorkflowFilter;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActivityInterface
public interface TemporalActivity {
  String QUEUE_NAME = "TemporalActivityTaskQueue";
  int LIST_EXECUTION_PAGE_SIZE = 500;

  @ActivityMethod
  void signalWorkflows(SignalWorkflowsRequest request);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class SignalWorkflowsRequest {
    private WorkflowFilter filter;
    private String signalName;
    private Object[] payload;
  }
}
