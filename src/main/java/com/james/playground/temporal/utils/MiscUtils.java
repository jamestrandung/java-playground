package com.james.playground.temporal.utils;

import java.time.Duration;

public class MiscUtils {
  public static void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
