package com.james.playground.temporal.dsl.language.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.marketing.MarketingWorkflowDefinition;
import com.james.playground.temporal.dsl.language.versioning.WorkflowChangeSignal;
import java.util.Map;
import java.util.Optional;

@JsonTypeInfo(
    use = Id.NAME,
    property = WorkflowDefinitionType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MarketingWorkflowDefinition.class, name = WorkflowDefinitionType.MARKETING),
})
public interface WorkflowDefinition<T extends WorkflowDefinition<T>> {
  @JsonIgnore
  String getWorkflowIdPrefix();

  WorkflowNode findNodeIgnoringDeletedNodes(String nodeId);

  WorkflowNode findNodeAcceptingDeletedNode(String nodeId);

  Map<String, WorkflowNode> getNodes();

  Optional<WorkflowChangeSignal> detectChange(T other);
}
