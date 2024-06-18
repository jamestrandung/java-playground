package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.nodes.RandomDistributionNode;
import com.james.playground.temporal.dsl.language.nodes.RandomDistributionNode.RandomDistributionBranch;
import io.temporal.workflow.Workflow;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class RandomDistributionVisitor extends NodeVisitor<RandomDistributionNode> {
  private static final Logger LOGGER = Workflow.getLogger(RandomDistributionVisitor.class);

  public RandomDistributionVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public String visit(RandomDistributionNode node) {
    int randomIdx = Workflow.newRandom().nextInt(10000);

    int upperBound = 0;
    for (RandomDistributionBranch branch : node.getBranches()) {
      upperBound = upperBound + branch.getProbability();

      if (randomIdx < upperBound) {
        this.userGroupActivity.updateInMemoryCounter(
            UserGroupInput.builder()
                .groupId((long) branch.getProbability())
                .userId(this.input.getUserId())
                .build()
        );

        return branch.getNextNodeId();
      }
    }

    this.userGroupActivity.updateInMemoryCounter(
        UserGroupInput.builder()
            .groupId(10000L)
            .userId(this.input.getUserId())
            .build()
    );

    return node.getNextNodeId();
  }
}
