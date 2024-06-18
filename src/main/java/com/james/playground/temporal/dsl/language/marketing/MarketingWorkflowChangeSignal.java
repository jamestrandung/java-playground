package com.james.playground.temporal.dsl.language.marketing;

import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.marketing.MarketingWorkflowImpl;


public interface MarketingWorkflowChangeSignal extends WorkflowChangeSignal {
  void accept(MarketingWorkflowImpl workflow);
}
