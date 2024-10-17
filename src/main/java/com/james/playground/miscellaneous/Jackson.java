package com.james.playground.miscellaneous;

import com.fasterxml.jackson.core.type.TypeReference;
import com.james.playground.temporal.dsl.language.core.WorkflowNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode;
import com.james.playground.temporal.dsl.language.nodes.TransitNode.TransitCategory;
import com.james.playground.utils.FormatUtils;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

public class Jackson {
  private static final String BLANK_WORKFLOW_STRUCTURE = FormatUtils.toJsonString(
      EntryStream.of(
          TransitNode.START_ID,
          TransitNode.builder()
              .id(TransitNode.START_ID)
              .category(TransitCategory.ENTRY_EXIT)
              .nextNodeId(TransitNode.END_ID)
              .build(),
          TransitNode.END_ID,
          TransitNode.builder()
              .id(TransitNode.END_ID)
              .category(TransitCategory.ENTRY_EXIT)
              .nextNodeId(null)
              .build()
      ).toCustomMap(LinkedHashMap::new)
  );

  private static final String NODE_LIST = FormatUtils.toJsonString(
      StreamEx.of(
          TransitNode.START_ID,
          TransitNode.builder()
              .id(TransitNode.START_ID)
              .category(TransitCategory.ENTRY_EXIT)
              .nextNodeId(TransitNode.END_ID)
              .build(),
          TransitNode.END_ID,
          TransitNode.builder()
              .id(TransitNode.END_ID)
              .category(TransitCategory.ENTRY_EXIT)
              .nextNodeId(null)
              .build()
      ).toList()
  );

  public static void main(String[] args) throws IOException {
    System.out.println("HERE 1");
    System.out.println(FormatUtils.<Map<String, WorkflowNode>>fromJsonString(BLANK_WORKFLOW_STRUCTURE));

    System.out.println("HERE 2");
    Map<String, WorkflowNode> nodes = FormatUtils.fromJsonString(
        BLANK_WORKFLOW_STRUCTURE, new TypeReference<Map<String, WorkflowNode>>() {
        });

    System.out.println(nodes);

    System.out.println("HERE 3");
    WorkflowNode node = nodes.get("START");
    System.out.println(node);

    System.out.println("HERE 4");
  }
}
