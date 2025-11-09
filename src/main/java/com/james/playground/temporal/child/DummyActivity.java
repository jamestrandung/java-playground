package com.james.playground.temporal.child;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActivityInterface
public interface DummyActivity {
  String QUEUE_NAME = "DummyActivityTaskQueue";

  @ActivityMethod
  void consume(Request request);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class Request {
    private Collection<String> texts;
  }
}
