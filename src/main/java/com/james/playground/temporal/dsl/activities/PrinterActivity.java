package com.james.playground.temporal.dsl.activities;

import com.james.playground.temporal.dsl.dto.DynamicActivityResult;
import com.james.playground.temporal.dsl.language.nodes.PrinterNode;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActivityInterface
public interface PrinterActivity {
  String QUEUE_NAME = "PrinterTaskQueue";

  @ActivityMethod
  DynamicActivityResult print(PrinterInput input);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class PrinterInput {
    private PrinterNode node;
    private Long userId;
  }
}
