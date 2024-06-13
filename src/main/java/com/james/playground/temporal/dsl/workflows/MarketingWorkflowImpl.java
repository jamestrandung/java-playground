package com.james.playground.temporal.dsl.workflows;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.dto.MarketingWorkflowContext;
import com.james.playground.temporal.dsl.language.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.MarketingWorkflowStore;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflowImpl;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

@WorkflowImpl(taskQueues = MarketingWorkflow.QUEUE_NAME)
public class MarketingWorkflowImpl extends DynamicWorkflowImpl implements MarketingWorkflow {
  private MarketingWorkflowContext context;

  @Override
  protected void init(DynamicWorkflowInput input) {
    this.input = input;
    this.workflowDefinitionSupplier = () -> this.findWorkflowDefinition(this.input.getWorkflowDefinitionId());
    this.visitor = new DelegatingVisitor(this.input, this.workflowDefinitionSupplier);

    this.context = Workflow.sideEffect(
        MarketingWorkflowContext.class,
        () -> {
          MarketingWorkflowDefinition workflowDefinition = (MarketingWorkflowDefinition) this.workflowDefinitionSupplier.get();
          return workflowDefinition.getContext();
        }
    );
  }

  @Override
  protected boolean shouldExitEarly() {
    if (this.visitor.getBranchVisitor().isConditionMet(this.context.getExitCondition())) {
      return true;
    }

    return this.context.isShouldWithdrawIfEnrollmentConditionNotMet()
        && !this.visitor.getBranchVisitor().isConditionMet(this.context.getEnrollmentCondition());
  }

  WorkflowDefinition findWorkflowDefinition(String workflowDefinitionId) {
    return MarketingWorkflowStore.getInstance().findWorkflowDefinition(workflowDefinitionId);
  }
}
