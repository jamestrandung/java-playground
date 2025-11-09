package com.james.playground.temporal.child;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = DummyActivity.QUEUE_NAME)
public class DummyActivityImpl implements DummyActivity {
  @Override
  public void consume(Request request) {
    System.out.println(request.getTexts());
    System.out.println(request.getTexts().getClass());
  }
}
