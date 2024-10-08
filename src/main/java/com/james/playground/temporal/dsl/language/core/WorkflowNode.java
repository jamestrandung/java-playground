package com.james.playground.temporal.dsl.language.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.james.playground.temporal.dsl.language.core.NodeType.Constants;
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
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@JsonTypeInfo(
    use = Id.NAME,
    property = Constants.PROPERTY_NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransitNode.class, name = Constants.TRANSIT_VALUE),
    @JsonSubTypes.Type(value = PrinterNode.class, name = Constants.PRINTER_VALUE),
    @JsonSubTypes.Type(value = DelayNode.class, name = Constants.DELAY_VALUE),
    @JsonSubTypes.Type(value = RandomDistributionNode.class, name = Constants.RANDOM_DISTRIBUTION_VALUE),
    @JsonSubTypes.Type(value = SwitchNode.class, name = Constants.SWITCH_VALUE),
})
@ToString(callSuper = true)
public abstract class WorkflowNode {
  public static final String[] IGNORABLE_FIELDS_FOR_WORKFLOW_EXECUTION = {
      "deletable", Fields.launched
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
  private int indegree;
  private Long deletedOn;
  private boolean launched;

  @JsonIgnore
  public abstract NodeType getType();

  public abstract String accept(DelegatingVisitor visitor);

  public abstract Optional<NodeChangeSignal> detectChange(WorkflowNode latest);

  public boolean isDeletable() {
    return this.indegree <= 1 && this.canBeDeleted();
  }

  protected abstract boolean canBeDeleted();

  @JsonIgnore
  public boolean isDeleted() {
    return this.deletedOn != null;
  }
}
