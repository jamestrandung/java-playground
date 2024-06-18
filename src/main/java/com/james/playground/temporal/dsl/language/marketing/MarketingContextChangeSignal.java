package com.james.playground.temporal.dsl.language.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignalType;
import com.james.playground.temporal.dsl.workflows.marketing.MarketingWorkflowImpl;

public class MarketingContextChangeSignal implements MarketingWorkflowChangeSignal {
  @JsonProperty(WorkflowChangeSignalType.PROPERTY_NAME)
  private final String type = WorkflowChangeSignalType.MARKETING_CONTEXT;

  @Override
  public void accept(MarketingWorkflowImpl workflow) {
    workflow.visit(this);
  }
}
