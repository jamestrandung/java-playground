package com.james.playground.temporal.determinism;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import java.util.List;

@ActivityInterface
public interface PrintActivity {
  String QUEUE_NAME = "PrintTaskQueue";

  @ActivityMethod
  void print(List<String> strings);

  @ActivityMethod
  void echo(List<String> strings);
}
