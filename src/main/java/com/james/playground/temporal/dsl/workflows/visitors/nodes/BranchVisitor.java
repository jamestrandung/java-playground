package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.Condition;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.language.nodes.BranchNode;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class BranchVisitor extends NodeVisitor<BranchNode> {
  private static final Logger LOGGER = Workflow.getLogger(BranchVisitor.class);

  public BranchVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public String visit(BranchNode node) {
    for (Condition condition : node.getConditions()) {
      if (condition.accept(this)) {
        return condition.getNextNodeId();
      }
    }

    return node.getNextNodeId();
  }

  public boolean visit(GroupMembershipCondition condition) {
    return this.userGroupActivity.isUserInGroup(
        UserGroupInput.builder()
            .userId(this.input.getUserId())
            .groupId(condition.getGroupId())
            .build()
    );
  }
}
