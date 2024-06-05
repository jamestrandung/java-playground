package com.james.playground.temporal.dsl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicWorkflowInput {
  private String workflowDefinitionId;
  private Long userId;
}
