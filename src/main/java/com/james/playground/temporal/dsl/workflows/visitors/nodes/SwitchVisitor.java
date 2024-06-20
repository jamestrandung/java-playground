package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.language.core.Condition;
import com.james.playground.temporal.dsl.language.nodes.SwitchNode;
import com.james.playground.temporal.dsl.language.nodes.SwitchNode.SwitchCase;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class SwitchVisitor extends NodeVisitor<SwitchNode> {
  private static final Logger LOGGER = Workflow.getLogger(SwitchVisitor.class);

  public SwitchVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public String visit(SwitchNode node) {
    for (SwitchCase c : node.getCases()) {
      if (this.isConditionMet(c.getCondition())) {
        return c.getNextNodeId();
      }
    }

    return node.getNextNodeId();
  }

  public boolean isConditionMet(Condition condition) {
    return condition != null && condition.accept(this);
  }

  public boolean visit(GroupMembershipCondition condition) {
    boolean isUserInGroup = this.localUserGroupActivity.isUserInGroup(
        UserGroupInput.builder()
            .userId(this.input.getUserId())
            .groupId(condition.getGroupId())
            .build()
    );

    return isUserInGroup == condition.isShouldBeInGroup();
  }
}
