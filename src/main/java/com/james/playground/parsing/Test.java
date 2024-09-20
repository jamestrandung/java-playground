package com.james.playground.parsing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.james.playground.temporal.dsl.language.nodes.delay.DelayNode;
import com.james.playground.utils.FormatUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import lombok.experimental.SuperBuilder;

public class Test {
  public static void main(String[] args) {
    //    String dateTime = "2024-06-19T20:49:00";
    //    System.out.println(ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    //
    //    String timeOfDay = "10:15:30";
    //    System.out.println(OffsetTime.parse(timeOfDay, DateTimeFormatter.ISO_OFFSET_TIME));

    DelayNode node = DelayNode.builder()
        .activeGroupId(123L)
        .releaseDateTime("2024-06-19T20:49:00")
        .build();

    System.out.println(FormatUtils.toJsonString(node));

    DelayNodeView view = DelayNodeView.builder()
        .node(node)
        .activeGroupCount(1L)
        .build();

    System.out.println(FormatUtils.toJsonString(view));
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  public static class DelayNodeView extends DelayNode {
    @Delegate
    @JsonIgnore
    private DelayNode node;
    private Long activeGroupCount;
  }
}
