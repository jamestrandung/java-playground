package com.james.playground.temporal.properties;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@WorkflowInterface
public interface PropertyWorkflow {
  String QUEUE_NAME = "PropertyTaskQueue";

  @WorkflowMethod
  String execute(PropertyRequest request);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class PropertyRequest {
    private Properties properties;
  }
}
