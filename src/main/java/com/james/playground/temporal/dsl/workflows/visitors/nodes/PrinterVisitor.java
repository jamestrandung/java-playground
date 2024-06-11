package com.james.playground.temporal.dsl.workflows.visitors.nodes;

import com.james.playground.temporal.dsl.activities.PrinterActivity.PrinterInput;
import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import com.james.playground.temporal.dsl.dto.DynamicWorkflowInput;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import io.temporal.workflow.Workflow;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
@NoArgsConstructor
public class PrinterVisitor extends NodeVisitor<PrinterNode> {
  private static final Logger logger = Workflow.getLogger(PrinterVisitor.class);

  public PrinterVisitor(DynamicWorkflowInput input) {
    super(input);
  }

  @Override
  public String visit(PrinterNode node) {
    log.info("PrinterNode: {}", node);

    DynamicActivityResult result = this.printerActivity.print(
        PrinterInput.builder()
            .node(node)
            .userId(this.input.getUserId())
            .build()
    );

    return result.getNextNodeId();
  }
}
