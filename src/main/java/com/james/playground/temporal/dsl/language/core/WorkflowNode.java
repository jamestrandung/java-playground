package com.james.playground.temporal.dsl.language.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import com.james.playground.temporal.dsl.language.nodes.RandomDistributionNode;
import com.james.playground.temporal.dsl.language.nodes.SwitchNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayNode;
import com.james.playground.temporal.dsl.language.versioning.NodeChangeSignal;
import com.james.playground.temporal.dsl.workflows.visitors.DelegatingVisitor;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonTypeInfo(
    use = Id.NAME,
    property = NodeType.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransitNode.class, name = NodeType.TRANSIT),
    @JsonSubTypes.Type(value = PrinterNode.class, name = NodeType.PRINTER),
    @JsonSubTypes.Type(value = DelayNode.class, name = NodeType.DELAY),
    @JsonSubTypes.Type(value = RandomDistributionNode.class, name = NodeType.RANDOM_DISTRIBUTION),
    @JsonSubTypes.Type(value = SwitchNode.class, name = NodeType.SWITCH),
})
public abstract class WorkflowNode {
  public static final String[] IGNORABLE_FIELDS_FOR_WORKFLOW_EXECUTION = {
      Fields.deletable, Fields.activeInProduction
  };

  public static final Set<Class<? extends WorkflowNode>> EXISTING_IMPLEMENTATIONS = Set.of(
      TransitNode.class,
      PrinterNode.class,
      DelayNode.class,
      RandomDistributionNode.class,
      SwitchNode.class
  );

  private String id;
  private String nextNodeId;
  private Long deletedOn;

  private boolean deletable;
  private boolean activeInProduction;

  @JsonIgnore
  public abstract String getType();

  public abstract String accept(DelegatingVisitor visitor);

  public abstract Optional<NodeChangeSignal> detectChange(WorkflowNode latest);

  @JsonIgnore
  public boolean isDeleted() {
    return this.deletedOn != null;
  }
}
