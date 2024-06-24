package com.james.playground.temporal.dsl.workflows.marketing;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.dto.MarketingContext;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.marketing.MarketingContextChangeSignal;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowChangeSignal;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowStore;
import com.james.playground.temporal.dsl.workflows.core.DynamicWorkflowImpl;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.util.List;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = MarketingWorkflow.QUEUE_NAME)
public class MarketingWorkflowImpl extends DynamicWorkflowImpl<MarketingWorkflowChangeSignal> implements MarketingWorkflow {
  private static final Logger LOGGER = Workflow.getLogger(MarketingWorkflowImpl.class);

  private MarketingContext context;

  @Override
  protected void init(DynamicWorkflowInput input) {
    this.input = input;
    this.workflowDefinitionSupplier = () -> this.findWorkflowDefinition(this.input.getWorkflowDefinitionId());
    this.visitor = new DelegatingVisitor(this.input, this.workflowDefinitionSupplier);

    this.context = Workflow.sideEffect(
        MarketingContext.class,
        () -> {
          MarketingWorkflowDefinition workflowDefinition = (MarketingWorkflowDefinition) this.workflowDefinitionSupplier.get();
          return workflowDefinition.getContext();
        }
    );
  }

  @Override
  protected boolean shouldExitEarly() {
    if (this.context == null) {
      return false;
    }

    if (this.visitor.getSwitchVisitor().isConditionMet(this.context.getExitCondition())) {
      return true;
    }

    // Might get handled by delta sync
    return this.context.isShouldWithdrawIfEnrollmentConditionNotMet()
        && this.context.getEnrollmentCondition() != null
        && !this.visitor.getSwitchVisitor()
        .isConditionMet(this.context.getEnrollmentCondition());
  }

  @Override
  protected void handleWorkflowChangeSignals(List<MarketingWorkflowChangeSignal> workflowSignals) {
    for (MarketingWorkflowChangeSignal signal : workflowSignals) {
      signal.accept(this);
    }
  }

  public void visit(MarketingContextChangeSignal signal) {
    MarketingContext latest = Workflow.sideEffect(
        MarketingContext.class,
        () -> {
          MarketingWorkflowDefinition workflowDefinition = (MarketingWorkflowDefinition) this.workflowDefinitionSupplier.get();
          return workflowDefinition.getContext();
        }
    );

    if (this.context == null) {
      this.context = latest;
      return;
    }

    this.context.merge(latest);

    LOGGER.info("Latest context: {}", this.context);
  }

  WorkflowDefinition<?> findWorkflowDefinition(String workflowDefinitionId) {
    return MarketingWorkflowStore.getInstance().findWorkflowDefinition(workflowDefinitionId);
  }
}
