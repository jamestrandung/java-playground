package com.james.playground.temporal

import io.temporal.testing.TestActivityEnvironment
import spock.lang.Specification

abstract class BaseActivitySpecification<T> extends Specification {
    protected TestActivityEnvironment testEnv
    protected T activity

    def setup() {
        testEnv = TestActivityEnvironment.newInstance()
        testEnv.registerActivitiesImplementations(activityImplementationToRegister)

        activity = testEnv.newActivityStub(activityTypeToBuildStub)
    }

    def cleanup() {
        testEnv.close()
    }

    abstract <I> I getActivityImplementationToRegister()

    abstract Class<T> getActivityTypeToBuildStub()
}
