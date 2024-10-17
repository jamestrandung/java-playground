package com.james.playground.temporal.interceptor;

import com.james.playground.temporal.moneytransfer.activities.AccountActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.common.SearchAttributeKey;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;
import io.temporal.failure.ApplicationFailure;
import io.temporal.failure.CanceledFailure;
import io.temporal.failure.TerminatedFailure;
import io.temporal.workflow.Async;
import io.temporal.workflow.CompletablePromise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Most of the complexity of the implementation is due to the asynchronous nature of the activity invocation at the interceptor level.
 */
@Slf4j
public class RetryOnSignalWorkflowOutboundCallsInterceptor extends WorkflowOutboundCallsInterceptorBase {
  private static final RetryOptions RETRY_OPTIONS = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(3)) // Wait duration before first retry
      .setMaximumInterval(Duration.ofSeconds(60)) // Maximum wait duration between retries
      .setMaximumAttempts(3) // Maximum number of retry attempts
      .setBackoffCoefficient(1)
      .build();
  private static final ActivityOptions ACTIVITY_OPTIONS = ActivityOptions.newBuilder()
      // One of the following MUST be set, StartToCloseTimeout is a better option
      .setScheduleToCloseTimeout(Duration.ofSeconds(60)) // Max duration from scheduled to completion, including queue time
      .setStartToCloseTimeout(Duration.ofSeconds(10)) // Max execution time for single Activity
      .setRetryOptions(RETRY_OPTIONS)
      .build();
  /**
   * For the example brevity the interceptor fails or retries all activities that are waiting for an action. The production version might implement
   * retry and failure of specific activities by their type.
   */
  private final Set<ActivityRetryState<?>> pendingActivities = new HashSet<>();
  private final AccountActivity accountActivity = Workflow.newActivityStub(AccountActivity.class, ACTIVITY_OPTIONS, null);

  public RetryOnSignalWorkflowOutboundCallsInterceptor(WorkflowOutboundCallsInterceptor next) {
    super(next);

    log.info("Creating RetryOnSignalWorkflowOutboundCallsInterceptor, next: {}", next);

    // Registers the listener for retry and fail signals as well as getPendingActivitiesStatus
    // query. Register in the constructor to do it once per workflow instance.
    Workflow.registerListener(
        new RetryOnSignalInterceptorListener() {
          @Override
          public void retry() {
            var toIterate = new ArrayList<>(RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities);

            for (ActivityRetryState<?> pending : toIterate) {
              pending.retry();
            }
          }

          @Override
          public void fail() {
            var toIterate = new ArrayList<>(RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities);

            for (ActivityRetryState<?> pending : toIterate) {
              pending.fail();
            }
          }
        });
  }

  @Override
  public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {
    ActivityRetryState<R> retryState = new ActivityRetryState<R>(input);
    this.pendingActivities.add(retryState);

    log.info(
        "Executing activity, name: {}, args: {}, pending activity size: {}, header: {}",
        input.getActivityName(), input.getArgs(), this.pendingActivities.size(), input.getHeader().getValues()
    );

    return retryState.execute();
  }

  private enum Action {
    RETRY,
    FAIL
  }

  private class ActivityRetryState<R> {
    private final ActivityInput<R> input;
    private final CompletablePromise<R> asyncResult = Workflow.newPromise();
    private CompletablePromise<Action> action;

    private ActivityRetryState(ActivityInput<R> input) {
      this.input = input;
    }

    ActivityOutput<R> execute() {
      return this.executeWithAsyncRetry();
    }

    // Executes activity with retry based on signaled action asynchronously
    private ActivityOutput<R> executeWithAsyncRetry() {
      this.action = null;

      ActivityOutput<R> result = RetryOnSignalWorkflowOutboundCallsInterceptor.super.executeActivity(this.input);
      result.getResult()
          .handle(
              (r, failure) -> {
                if (failure == null) {
                  RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities.remove(this);
                  this.asyncResult.complete(r);
                  return null;
                }

                if (this.isNonRetryable(failure)) {
                  RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities.remove(this);
                  this.asyncResult.completeExceptionally(failure);
                  return null;
                }

                RetryOnSignalWorkflowOutboundCallsInterceptor.super.upsertTypedSearchAttributes(
                    SearchAttributeKey.forBoolean("CustomManualRetryRequired")
                        .valueSet(true)
                );

                log.info("Handling error for workflow: " + Workflow.getInfo().getWorkflowType());

                Async.procedure(
                    RetryOnSignalWorkflowOutboundCallsInterceptor.this.accountActivity::warn,
                    failure.getMessage()
                ).exceptionally(ex -> {
                  log.info(
                      "Failed to warn error, pending activity size: {}",
                      RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities.size()
                  );

                  return null;
                });

                // Asynchronously executes requested action when signal is received.
                this.action = Workflow.newPromise();
                return this.action.thenApply(
                    a -> {
                      RetryOnSignalWorkflowOutboundCallsInterceptor.super.upsertTypedSearchAttributes(
                          SearchAttributeKey.forBoolean("CustomManualRetryRequired")
                              .valueUnset()
                      );

                      switch (a) {
                        case RETRY -> this.executeWithAsyncRetry();
                        case FAIL -> {
                          RetryOnSignalWorkflowOutboundCallsInterceptor.this.pendingActivities.remove(this);
                          this.asyncResult.completeExceptionally(failure);
                        }
                      }

                      return null;
                    });
              });

      return new ActivityOutput<>(result.getActivityId(), this.asyncResult);
    }

    boolean isNonRetryable(Throwable ex) {
      if (ex instanceof ApplicationFailure casted && casted.isNonRetryable()) {
        return true;
      }

      if (ex instanceof CanceledFailure || ex instanceof TerminatedFailure) {
        return true;
      }

      Throwable rootCause = ExceptionUtils.getRootCause(ex);
      if (rootCause != null && rootCause != ex && this.isNonRetryable(rootCause)) {
        return true;
      }

      Throwable cause = ex.getCause();
      return cause != null && cause != ex && this.isNonRetryable(cause);
    }

    public void retry() {
      if (this.action == null) {
        return;
      }

      this.action.complete(Action.RETRY);
    }

    public void fail() {
      if (this.action == null) {
        return;
      }

      this.action.complete(Action.FAIL);
    }
  }
}
