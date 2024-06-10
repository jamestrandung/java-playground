package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.UserGroupActivity.UserGroupInput;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.RandomDistributionNode;
import io.temporal.workflow.Workflow;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class RandomDistributionVisitor extends NodeVisitor<RandomDistributionNode> {
  private static final Logger logger = Workflow.getLogger(RandomDistributionVisitor.class);

  public RandomDistributionVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public WorkflowNode visit(RandomDistributionNode node) {
    List<String> nextNodeIds = node.getNextNodeIds();

    int randomIdx = Workflow.newRandom().nextInt(nextNodeIds.size());

    this.userGroupActivity.updateInMemoryCounter(
        UserGroupInput.builder()
            .groupId((long) randomIdx)
            .userId(this.input.getUserId())
            .build()
    );

    return this.findNodeIgnoringDeletedNodes(nextNodeIds.get(randomIdx));
  }
}
