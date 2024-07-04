package com.james.playground.memoize;

import java.io.Serializable;

public class Functions {
  @FunctionalInterface
  public interface Func1<R, A1> extends Serializable {
    R apply(A1 a1);
  }

  @FunctionalInterface
  public interface Func<R> extends Serializable {
    R apply();
  }
}
