package com.james.playground.temporal

import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.testing.TestWorkflowEnvironment
import io.temporal.testing.WorkflowReplayer
import io.temporal.worker.Worker
import spock.lang.Specification

abstract class BaseWorkflowSpecification<T> extends Specification {
    protected static final String TEST_QUEUE = "test-queue"

    protected TestWorkflowEnvironment testEnv
    protected WorkflowClient client
    protected Worker worker

    protected T workflow

    def setup() {
        testEnv = TestWorkflowEnvironment.newInstance()
        client = testEnv.getWorkflowClient()

        worker = testEnv.newWorker(TEST_QUEUE)

        for (Object activityImplementation : activityImplementationsToRegister) {
            worker.registerActivitiesImplementations(activityImplementation)
        }

        worker.registerWorkflowImplementationTypes(workflowImplementationTypeToRegister)

        testEnv.start()

        workflow = client.newWorkflowStub(workflowTypeToBuildStub, WorkflowOptions.newBuilder().setTaskQueue(TEST_QUEUE).build())
    }

    def cleanup() {
        testEnv.close()
    }

    abstract List<Object> getActivityImplementationsToRegister()

    abstract Class<?> getWorkflowImplementationTypeToRegister()

    abstract Class<T> getWorkflowTypeToBuildStub()

    void replayWorkflowExecution(File eventHistoryFile) {
        WorkflowReplayer.replayWorkflowExecution(eventHistoryFile, workflowImplementationTypeToRegister)
    }
}
