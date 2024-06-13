package com.james.playground.temporal.dsl.language;

import com.james.playground.temporal.dsl.dto.MarketingWorkflowContext;
import com.james.playground.temporal.dsl.language.core.WorkflowDefinition;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import java.util.Collections;
import java.util.Map;
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
public class MarketingWorkflowDefinition implements WorkflowDefinition {
  public static final MarketingWorkflowDefinition BLANK = MarketingWorkflowDefinition.builder()
      .nodes(Collections.emptyMap())
      .build();

  private String id;
  private MarketingWorkflowContext context;
  private Map<String, WorkflowNode> nodes;

  public WorkflowNode put(String nodeId, WorkflowNode node) {
    return this.nodes.put(nodeId, node);
  }

  public WorkflowNode findNodeIgnoringDeletedNodes(String nodeId) {
    log.info("Looking for alive node, workflowDefinition ID: {}, node ID: {}", this.id, nodeId);
    String lookUpNodeId = nodeId;
    while (true) {
      WorkflowNode result = this.nodes.get(lookUpNodeId);

      if (result == null) {
        throw new RuntimeException("Node not found, " + "workflowDefinitionId: " + this.id + ", nodeId: " + lookUpNodeId);
      }

      if (result.isDeleted()) {
        lookUpNodeId = result.getNextNodeId();
        continue;
      }

      return result;
    }
  }

  public WorkflowNode findNodeAcceptingDeletedNode(String nodeId) {
    log.info("Looking for node, workflowDefinition ID: {}, node ID: {}", this.id, nodeId);
    WorkflowNode result = this.nodes.get(nodeId);

    if (result == null) {
      throw new RuntimeException("Node not found, " + "workflowDefinitionId: " + this.id + ", nodeId: " + nodeId);
    }

    return result;
  }
}
