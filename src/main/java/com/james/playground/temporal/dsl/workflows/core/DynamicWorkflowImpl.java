package com.james.playground.temporal.dsl.workflows.core;

import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition.ExecutablePath;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import io.temporal.workflow.Workflow;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;

@Slf4j
public abstract class DynamicWorkflowImpl<T extends WorkflowChangeSignal> implements DynamicWorkflow<T> {
  public static final String CHANGE_SIGNAL_NAME = "handleChangeSignals";
  private static final Logger LOGGER = Workflow.getLogger(DynamicWorkflowImpl.class);

  protected DynamicWorkflowInput input;
  protected Supplier<WorkflowDefinition<?>> workflowDefinitionSupplier;
  protected DelegatingVisitor visitor;

  @Override
  public void execute(DynamicWorkflowInput input) {
    this.init(input);

    String nextNodeId = TransitNode.START_ID;
    while (StringUtils.isNotBlank(nextNodeId)) {
      WorkflowNode node = this.findFirstExecutableNode(nextNodeId);
      if (node == null) {
        break;
      }

      log.info("Node: {}", node);
      nextNodeId = node.accept(this.visitor);

      if (NodeType.DELAY.equals(node.getType()) && this.shouldExitEarly()) {
        break;
      }
    }
  }

  @Override
  public void handleChangeSignals(List<T> workflowSignals, List<NodeChangeSignal> nodeSignals) {
    this.handleWorkflowChangeSignals(ListUtils.emptyIfNull(workflowSignals));

    for (NodeChangeSignal signal : ListUtils.emptyIfNull(nodeSignals)) {
      signal.accept(this.visitor);
    }
  }

  protected abstract void init(DynamicWorkflowInput input);

  protected abstract boolean shouldExitEarly();

  protected abstract void handleWorkflowChangeSignals(List<T> workflowSignals);

  WorkflowNode findFirstExecutableNode(String nodeId) {
    ExecutablePath result = Workflow.sideEffect(
        ExecutablePath.class,
        () -> this.workflowDefinitionSupplier.get()
            .findFirstExecutableNode(nodeId)
    );

    return CollectionUtils.isEmpty(result) ? null : result.getExecutableNode();
  }
}
