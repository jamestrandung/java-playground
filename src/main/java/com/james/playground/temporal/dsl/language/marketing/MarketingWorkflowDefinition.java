package com.james.playground.temporal.dsl.language.marketing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.james.playground.temporal.dsl.dto.MarketingContext;
import com.james.playground.temporal.dsl.dto.RecurringSchedule;
import com.james.playground.temporal.dsl.language.core.NodeType;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinitionType;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import com.james.playground.temporal.dsl.workflows.marketing.MarketingWorkflow;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketingWorkflowDefinition implements WorkflowDefinition<MarketingWorkflowDefinition> {
  public static final MarketingWorkflowDefinition BLANK = MarketingWorkflowDefinition.builder()
      .nodes(Collections.emptyMap())
      .build();

  @JsonProperty(WorkflowDefinitionType.PROPERTY_NAME)
  private final String type = WorkflowDefinitionType.MARKETING;

  private String id;
  private RecurringSchedule schedule;
  private MarketingContext context;
  private Map<String, WorkflowNode> nodes;

  public WorkflowNode put(String nodeId, WorkflowNode node) {
    return this.nodes.put(nodeId, node);
  }

  @Override
  public String getWorkflowIdPrefix() {
    return String.format(MarketingWorkflow.WORKFLOW_ID_PREFIX, this.id);
  }

  public ExecutablePath findFirstExecutableNode(String nodeId) {
    log.info("Looking for alive node, workflowDefinition ID: {}, node ID: {}", this.id, nodeId);
    ExecutablePath result = new ExecutablePath();

    String lookUpNodeId = nodeId;
    while (true) {
      WorkflowNode node = this.nodes.get(lookUpNodeId);

      if (node == null) {
        throw new RuntimeException("Node not found, " + "workflowDefinitionId: " + this.id + ", nodeId: " + lookUpNodeId);
      }

      result.addFirst(node);

      boolean isTransitButNotEndingNode = NodeType.TRANSIT.equals(node.getType()) && !TransitNode.END_ID.equals(node.getId());
      if (node.isDeleted() || isTransitButNotEndingNode) {
        lookUpNodeId = node.getNextNodeId();
        continue;
      }

      break;
    }

    return result;
  }

  public WorkflowNode findNodeAcceptingDeletedNode(String nodeId) {
    log.info("Looking for node, workflowDefinition ID: {}, node ID: {}", this.id, nodeId);
    WorkflowNode result = this.nodes.get(nodeId);

    if (result == null) {
      throw new RuntimeException("Node not found, " + "workflowDefinitionId: " + this.id + ", nodeId: " + nodeId);
    }

    return result;
  }

  @Override
  public Optional<WorkflowChangeSignal> detectChange(MarketingWorkflowDefinition other) {
    if (!Objects.equals(this.context, other.context)) {
      return Optional.of(new MarketingContextChangeSignal());
    }

    return Optional.empty();
  }

  public static class ExecutablePath extends LinkedList<WorkflowNode> {
    public WorkflowNode getExecutableNode() {
      WorkflowNode result = this.getFirst();
      if (result == null || result.isDeleted() || TransitNode.END_ID.equals(result.getId())) {
        return null;
      }

      return result;
    }
  }
}
