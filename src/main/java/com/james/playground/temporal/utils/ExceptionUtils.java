package com.james.playground.temporal.utils;

import java.util.function.Predicate;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {
  public static <T extends Throwable> void swallowExceptionIf(T throwable, Predicate<T> condition) throws T {
    if (!condition.test(throwable)) {
      throw throwable;
    }
  }
}
