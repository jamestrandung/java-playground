package com.james.playground.temporal.interceptor;

import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;

/** Should be registered through WorkerFactoryOptions. */
public class RetryOnSignalWorkerInterceptor implements WorkerInterceptor {
  @Override
  public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
    return new RetryOnSignalWorkflowInboundCallsInterceptor(next);
  }

  @Override
  public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
    return next;
  }
}
