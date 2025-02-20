package com.james.playground.utils;

public class ConcurrenUtils {
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (Exception ex) {
      
    }
  }
}
