package com.james.playground.temporal.interceptor;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;

public interface RetryOnSignalInterceptorListener {
  /** Requests retry of the activities waiting after failure. */
  @SignalMethod
  void retry();

  /** Requests no more retries of the activities waiting after failure. */
  @SignalMethod
  void fail();
}
