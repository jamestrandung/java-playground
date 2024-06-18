package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.conditions.GroupMembershipCondition;
import com.james.playground.temporal.dsl.language.core.Condition;
import com.james.playground.temporal.dsl.language.nodes.BranchNode;
import io.temporal.workflow.Workflow;
import java.util.Map;
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
    for (Map.Entry<String, Condition> entry : node.getConditions().entrySet()) {
      if (entry.getValue().accept(this)) {
        return entry.getKey();
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
